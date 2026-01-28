"use client";

import { useState, useMemo } from "react";
import { useFields } from "@/hooks/useFields";
import { useTreatments } from "@/hooks/useTreatment";
import { ProductAutocomplete } from "@/components/journal/product-autocomplete";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
    Table, TableBody, TableCell, TableHead, TableHeader, TableRow
} from "@/components/ui/table";
import {
    Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogDescription
} from "@/components/ui/dialog";
import {
    Select, SelectContent, SelectItem, SelectTrigger, SelectValue
} from "@/components/ui/select";
import {
    Tooltip, TooltipContent, TooltipProvider, TooltipTrigger,
} from "@/components/ui/tooltip";
import { Badge } from "@/components/ui/badge";
import { AlertTriangle, Info, Plus, Trash2, Sprout, Bug, FlaskConical, Calendar, Tractor, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { Separator } from "@/components/ui/separator";
import { cn } from "@/lib/utils";
import { client } from "@/lib/api-client";

interface ProductUsageFromApi {
    cropName: string;
    pestName: string;
    dose: string;
}

interface ProductFromApi {
    id: string;
    name: string;
    activeSubstances: string[];
    usages: ProductUsageFromApi[];
}

interface ProductData {
    name: string;
    activeSubstance: string;
    authorizedPests: string[];
    doses: Record<string, string>;
}

interface MixItem {
    id: number;
    product: ProductData;
    targetPest: string;
    dose: string;
    isOffLabel: boolean;
}

export default function JournalPage() {
    const { data: fields } = useFields();
    const { treatments, addTreatment, deleteTreatment, isLoading } = useTreatments();

    const [isOpen, setIsOpen] = useState(false);
    const [isSaving, setIsSaving] = useState(false);
    const [isLoadingDetails, setIsLoadingDetails] = useState(false);

    const [header, setHeader] = useState({
        date: new Date().toISOString().split('T')[0],
        fieldId: "",
        description: ""
    });

    const [mixItems, setMixItems] = useState<MixItem[]>([]);

    const [tempRow, setTempRow] = useState<{
        product: ProductData | null;
        pest: string;
        dose: string;
    }>({
        product: null,
        pest: "",
        dose: ""
    });

    const compatibility = useMemo(() => {
        if (!tempRow.product) return { isMatch: true, suggestedDose: "", isKnownPest: false };

        const { product, pest } = tempRow;
        const isKnownPest = product.authorizedPests.includes(pest);
        const suggestedDose = isKnownPest ? product.doses[pest] : "";

        return {
            isMatch: isKnownPest,
            suggestedDose,
            isKnownPest
        };
    }, [tempRow.product, tempRow.pest]);

    const handleProductSelect = async (selection: { id: string; name: string }) => {
        setIsLoadingDetails(true);
        try {
            const productDetails = await client<ProductFromApi>(`/api/products/${selection.id}`);

            const selectedField = fields?.find(f => f.id.toString() === header.fieldId);

            const relevantUsages = productDetails.usages.filter(usage =>
                usage.cropName.toLowerCase()
            );

            const usagesToProcess = relevantUsages.length > 0 ? relevantUsages : productDetails.usages;

            const pestsList = Array.from(new Set(usagesToProcess.map(u => u.pestName)));

            const dosesMap: Record<string, string> = {};
            usagesToProcess.forEach(u => {
                if (!dosesMap[u.pestName]) {
                    dosesMap[u.pestName] = u.dose;
                }
            });

            if (selectedField && relevantUsages.length === 0 && fieldCrop) {
                toast.warning(`Ten środek nie posiada rejestracji dla uprawy: ${selectedField.crop}`);
            }

            setTempRow({
                product: {
                    name: productDetails.name,
                    activeSubstance: productDetails.activeSubstances.join(", "),
                    authorizedPests: pestsList,
                    doses: dosesMap
                },
                pest: "",
                dose: ""
            });

        } catch (error) {
            console.error(error);
            toast.error("Nie udało się pobrać szczegółów dawkowania");
        } finally {
            setIsLoadingDetails(false);
        }
    };

    const handlePestChange = (pestName: string) => {
        setTempRow(prev => ({
            ...prev,
            pest: pestName,
        }));
    };

    const addToMix = () => {
        if (!tempRow.product || !tempRow.pest || !tempRow.dose) {
            toast.warning("Uzupełnij środek, cel i dawkę");
            return;
        }

        const newItem: MixItem = {
            id: Date.now(),
            product: tempRow.product!,
            targetPest: tempRow.pest,
            dose: tempRow.dose,
            isOffLabel: !compatibility.isMatch
        };

        setMixItems(prev => [...prev, newItem]);
        setTempRow({ product: null, pest: "", dose: "" });
    };

    const removeFromMix = (id: number) => {
        setMixItems(prev => prev.filter(item => item.id !== id));
    };

    const handleSave = async () => {
        if (!header.fieldId) {
            toast.error("Wybierz pole uprawne");
            return;
        }
        if (mixItems.length === 0) {
            toast.error("Dodaj przynajmniej jeden środek do mieszaniny");
            return;
        }

        setIsSaving(true);
        try {
            const payload = {
                date: header.date,
                fieldId: Number(header.fieldId),
                description: header.description,
                items: mixItems.map(item => ({
                    productName: item.product.name,
                    activeSubstance: item.product.activeSubstance,
                    targetPest: item.targetPest,
                    dose: item.dose,
                    isOffLabel: item.isOffLabel
                }))
            };

            await addTreatment(payload);

            toast.success("Zabieg został zapisany w ewidencji");

            setIsOpen(false);
            setMixItems([]);
            setHeader({
                date: new Date().toISOString().split('T')[0],
                fieldId: "",
                description: ""
            });

        } catch (error) {
            console.error(error);
            toast.error("Błąd zapisu. Spróbuj ponownie.");
        } finally {
            setIsSaving(false);
        }
    };

    const handleDelete = async (id: number) => {
        try {
            await deleteTreatment(id);
            toast.success("Zabieg został usunięty");
        } catch (e) {
            toast.error("Nie udało się usunąć zabiegu");
        }
    };

    return (
        <div className="p-6 space-y-6">
            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                <div>
                    <h1 className="text-3xl font-bold">Dziennik Polowy</h1>
                    <p className="text-muted-foreground">Ewidencja zabiegów ochrony roślin.</p>
                </div>
                <Button onClick={() => setIsOpen(true)} className="bg-green-600 hover:bg-green-700 w-full sm:w-auto">
                    <Plus className="mr-2 h-4 w-4" /> Nowy zabieg
                </Button>
            </div>

            <Dialog open={isOpen} onOpenChange={setIsOpen}>
                <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
                    <DialogHeader>
                        <DialogTitle>Rejestracja zabiegu</DialogTitle>
                        <DialogDescription>
                            Kreator mieszaniny zbiornikowej. System zweryfikuje zgodność środków z celami.
                        </DialogDescription>
                    </DialogHeader>

                    <div className="grid gap-6 py-4">
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                            <div className="space-y-2">
                                <Label>Data zabiegu</Label>
                                <Input type="date" value={header.date} onChange={e => setHeader({...header, date: e.target.value})} />
                            </div>
                            <div className="space-y-2">
                                <Label>Pole uprawne</Label>
                                <Select onValueChange={v => setHeader({...header, fieldId: v})}>
                                    <SelectTrigger><SelectValue placeholder="Wybierz pole..." /></SelectTrigger>
                                    <SelectContent>
                                        {fields?.map(f => <SelectItem key={f.id} value={f.id.toString()}>{f.name}</SelectItem>)}
                                    </SelectContent>
                                </Select>
                            </div>
                        </div>

                        <Separator />

                        <div className="space-y-4 bg-slate-50 p-4 rounded-lg border shadow-sm">
                            <div className="flex items-center gap-2 mb-2">
                                <FlaskConical className="h-5 w-5 text-blue-600" />
                                <h3 className="font-semibold text-sm text-slate-700">Składniki mieszaniny</h3>
                            </div>

                            <div className="grid grid-cols-1 gap-4">
                                <div className="space-y-1.5 w-full">
                                    <Label className="text-xs font-semibold text-slate-500">1. Środek</Label>
                                    <div className="relative">
                                        <ProductAutocomplete onSelect={handleProductSelect} />
                                        {isLoadingDetails && (
                                            <div className="absolute right-2 top-1/2 -translate-y-1/2">
                                                <Loader2 className="h-4 w-4 animate-spin text-muted-foreground" />
                                            </div>
                                        )}
                                    </div>
                                </div>

                                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 items-end">
                                    <div className="space-y-1.5">
                                        <Label className="text-xs font-semibold text-slate-500">2. Cel (Agrofag)</Label>
                                        <Select
                                            disabled={!tempRow.product || isLoadingDetails}
                                            value={tempRow.pest}
                                            onValueChange={handlePestChange}
                                        >
                                            <SelectTrigger className={cn(
                                                "w-full transition-colors",
                                                !compatibility.isMatch && tempRow.pest ? "border-yellow-400 bg-yellow-50 text-yellow-900" : ""
                                            )}>
                                                <SelectValue placeholder={tempRow.product ? "Wybierz..." : "-"} />
                                            </SelectTrigger>
                                            <SelectContent>
                                                {tempRow.product?.authorizedPests.map(pest => (
                                                    <SelectItem key={pest} value={pest}>{pest}</SelectItem>
                                                ))}
                                                <Separator className="my-1"/>
                                                <SelectItem value="Inny (Off-label)">Inny / Własny cel...</SelectItem>
                                            </SelectContent>
                                        </Select>
                                    </div>

                                    <div className="space-y-1.5">
                                        <Label className="text-xs font-semibold text-slate-500">3. Dawka</Label>
                                        <div className="flex gap-2 w-full">
                                            <Input
                                                placeholder="Ilość/ha"
                                                value={tempRow.dose}
                                                onChange={e => setTempRow({...tempRow, dose: e.target.value})}
                                                disabled={!tempRow.product}
                                                className="min-w-[80px] flex-1"
                                            />

                                            <div className="flex shrink-0 gap-1">
                                                {!compatibility.isMatch && tempRow.pest && (
                                                    <TooltipProvider>
                                                        <Tooltip delayDuration={0}>
                                                            <TooltipTrigger asChild>
                                                                <div className="flex items-center justify-center w-10 h-10 bg-yellow-100 rounded-md cursor-help text-yellow-600 border border-yellow-200">
                                                                    <AlertTriangle className="h-5 w-5" />
                                                                </div>
                                                            </TooltipTrigger>
                                                            <TooltipContent side="top" className="bg-yellow-50 border-yellow-200 text-yellow-900 z-50">
                                                                <p className="font-bold">Zastosowanie poza etykietą!</p>
                                                            </TooltipContent>
                                                        </Tooltip>
                                                    </TooltipProvider>
                                                )}

                                                {compatibility.isMatch && tempRow.pest && (
                                                    <TooltipProvider>
                                                        <Tooltip delayDuration={0}>
                                                            <TooltipTrigger asChild>
                                                                <div className="flex items-center justify-center w-10 h-10 bg-green-100 rounded-md cursor-help text-green-600 border border-green-200">
                                                                    <Info className="h-5 w-5" />
                                                                </div>
                                                            </TooltipTrigger>
                                                            <TooltipContent side="top" className="bg-green-50 border-green-200 text-green-900 z-50">
                                                                <p><strong>{compatibility.suggestedDose}</strong></p>
                                                            </TooltipContent>
                                                        </Tooltip>
                                                    </TooltipProvider>
                                                )}

                                                <Button onClick={addToMix} size="icon" className="w-10 h-10 bg-blue-600 hover:bg-blue-700 shadow-sm" disabled={isLoadingDetails}>
                                                    <Plus className="h-5 w-5" />
                                                </Button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        {mixItems.length > 0 && (
                            <div className="border rounded-md overflow-hidden bg-white">
                                <Table>
                                    <TableHeader>
                                        <TableRow className="bg-slate-100/50">
                                            <TableHead>Środek</TableHead>
                                            <TableHead>Cel</TableHead>
                                            <TableHead>Dawka</TableHead>
                                            <TableHead className="text-center">Status</TableHead>
                                            <TableHead className="w-[50px]"></TableHead>
                                        </TableRow>
                                    </TableHeader>
                                    <TableBody>
                                        {mixItems.map((item) => (
                                            <TableRow key={item.id}>
                                                <TableCell className="font-medium">
                                                    {item.product.name}
                                                    <div className="text-[10px] text-muted-foreground">{item.product.activeSubstance}</div>
                                                </TableCell>
                                                <TableCell>
                                                    <div className="flex items-center gap-2">
                                                        <Bug className="h-3 w-3 text-slate-400" />
                                                        {item.targetPest === "Inny (Off-label)" ? "Cel niestandardowy" : item.targetPest}
                                                    </div>
                                                </TableCell>
                                                <TableCell>{item.dose}/ha</TableCell>
                                                <TableCell className="text-center">
                                                    {item.isOffLabel ? (
                                                        <Badge variant="outline" className="border-yellow-500 text-yellow-700 bg-yellow-50">Off-label</Badge>
                                                    ) : (
                                                        <Badge variant="outline" className="border-green-500 text-green-700 bg-green-50">Zgodny</Badge>
                                                    )}
                                                </TableCell>
                                                <TableCell>
                                                    <Button variant="ghost" size="sm" onClick={() => removeFromMix(item.id)}>
                                                        <Trash2 className="h-4 w-4 text-red-500" />
                                                    </Button>
                                                </TableCell>
                                            </TableRow>
                                        ))}
                                    </TableBody>
                                </Table>
                            </div>
                        )}
                    </div>

                    <DialogFooter>
                        <Button variant="outline" onClick={() => setIsOpen(false)} disabled={isSaving}>Anuluj</Button>
                        <Button onClick={handleSave} className="bg-green-600 hover:bg-green-700" disabled={isSaving}>
                            {isSaving && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                            Zapisz Zabieg
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            <div className="border rounded-lg bg-white shadow-sm overflow-hidden">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead className="w-[120px]">Data</TableHead>
                            <TableHead className="w-[180px]">Pole</TableHead>
                            <TableHead>Mieszanina (Środki + Cele)</TableHead>
                            <TableHead className="text-right w-[120px]">Status</TableHead>
                            <TableHead className="w-[50px]"></TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {isLoading ? (
                            <TableRow>
                                <TableCell colSpan={5} className="h-24 text-center">
                                    <Loader2 className="h-6 w-6 animate-spin mx-auto text-green-600" />
                                </TableCell>
                            </TableRow>
                        ) : treatments.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={5} className="h-40 text-center text-muted-foreground">
                                    <div className="flex flex-col items-center gap-3">
                                        <Sprout className="h-10 w-10 opacity-20" />
                                        <p>Brak wpisów w ewidencji. Dodaj pierwszy zabieg.</p>
                                    </div>
                                </TableCell>
                            </TableRow>
                        ) : (
                            treatments.map((t) => (
                                <TableRow key={t.id} className="align-top hover:bg-slate-50">
                                    <TableCell className="font-medium whitespace-nowrap">
                                        <div className="flex items-center gap-2 py-1">
                                            <Calendar className="h-4 w-4 text-slate-400" />
                                            {t.date}
                                        </div>
                                    </TableCell>
                                    <TableCell>
                                        <div className="flex items-center gap-2 py-1">
                                            <Tractor className="h-4 w-4 text-green-600" />
                                            {t.fieldName}
                                        </div>
                                    </TableCell>
                                    <TableCell>
                                        <div className="flex flex-col gap-2">
                                            {t.items.map((item, idx) => (
                                                <div key={idx} className="flex flex-col sm:flex-row sm:items-center justify-between text-sm border-b last:border-0 pb-1 last:pb-0 border-dashed border-slate-200">
                                                    <span className="font-medium text-slate-700">{item.productName}</span>
                                                    <div className="flex items-center gap-2 mt-1 sm:mt-0">
                                                        <span className="text-xs text-muted-foreground flex items-center gap-1">
                                                            <Bug className="h-3 w-3" /> {item.targetPest}
                                                        </span>
                                                        <Badge variant="secondary" className="text-[10px] h-5 px-1.5 ml-2">
                                                            {item.dose}/ha
                                                        </Badge>
                                                        {item.isOffLabel && (
                                                            <TooltipProvider>
                                                                <Tooltip>
                                                                    <TooltipTrigger><AlertTriangle className="h-3 w-3 text-yellow-500" /></TooltipTrigger>
                                                                    <TooltipContent><p>Off-label</p></TooltipContent>
                                                                </Tooltip>
                                                            </TooltipProvider>
                                                        )}
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    </TableCell>
                                    <TableCell className="text-right">
                                        <Badge className="bg-green-100 text-green-700 hover:bg-green-200 shadow-none border border-green-200">
                                            Wykonano
                                        </Badge>
                                    </TableCell>
                                    <TableCell className="text-right">
                                        <Button
                                            variant="ghost"
                                            size="icon"
                                            className="text-muted-foreground hover:text-red-500 hover:bg-red-50"
                                            onClick={() => handleDelete(t.id)}
                                        >
                                            <Trash2 className="h-4 w-4" />
                                        </Button>
                                    </TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
            </div>
        </div>
    );
}