"use client";

import { useState } from "react";
import { useFields } from "@/hooks/useFields";
import { Button } from "@/components/ui/button";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
    DialogDescription
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { Loader2, Plus, Trash2, MapPin, Sprout, Ruler, MoreHorizontal, ArrowRight } from "lucide-react";
import { SidebarTrigger } from "@/components/ui/sidebar";
import { Separator } from "@/components/ui/separator";
import {
    Breadcrumb,
    BreadcrumbItem,
    BreadcrumbList,
    BreadcrumbPage,
    BreadcrumbSeparator
} from "@/components/ui/breadcrumb";
import Link from "next/link";
import { TerytParcelSelector } from "@/components/fields/teryt-parcel-selector";
import { toast } from "sonner";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

export default function FieldsPage() {
    const { data: rawFields, isLoading, addField, deleteField } = useFields();
    const fields = Array.isArray(rawFields) ? rawFields : [];

    const [isOpen, setIsOpen] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [form, setForm] = useState({ name: "", area: "", description: "" });
    const [selectedParcels, setSelectedParcels] = useState<string[]>([]);

    const totalArea = fields.reduce((acc, curr) => acc + (curr.area || 0), 0);

    const handleAddParcel = (fullId: string) => {
        if (!selectedParcels.includes(fullId)) {
            setSelectedParcels([...selectedParcels, fullId]);
            toast.success("Dodano działkę do listy");
        } else {
            toast.info("Ta działka jest już na liście");
        }
    };

    const handleRemoveParcel = (idToRemove: string) => {
        setSelectedParcels(selectedParcels.filter(id => id !== idToRemove));
    };

    const handleSubmit = async () => {
        const areaValue = parseFloat(form.area);

        if (isNaN(areaValue) || areaValue <= 0) {
            toast.error("Powierzchnia pola musi być większa od 0 ha");
            return;
        }

        if (selectedParcels.length === 0) {
            toast.error("Musisz dodać przynajmniej jedną działkę ewidencyjną");
            return;
        }
        // ---------------------------------------------

        setIsSubmitting(true);
        try {
            await addField({
                name: form.name,
                area: areaValue,
                description: form.description,
                parcelIds: selectedParcels
            });

            setIsOpen(false);
            setForm({ name: "", area: "", description: "" });
            setSelectedParcels([]);
            toast.success("Pole zostało dodane pomyślnie");
        } catch (error: any) {
            console.error(error);
            toast.error(error.message || "Nie udało się dodać pola");
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleDelete = async (id: number, name: string) => {
        toast.promise(deleteField(id), {
            loading: 'Usuwanie pola...',
            success: `Pole "${name}" zostało usunięte`,
            error: 'Nie udało się usunąć pola'
        });
    };

    const isFormValid =
        form.name.trim().length > 0 &&
        parseFloat(form.area) > 0 &&
        selectedParcels.length > 0;

    return (
        <>
            <header className="flex h-16 shrink-0 items-center gap-2 px-4 transition-[width,height] ease-linear group-has-[[data-collapsible=icon]]/sidebar-wrapper:h-12">
                <SidebarTrigger className="-ml-1" />
                <Separator orientation="vertical" className="mr-2 h-4" />
                <Breadcrumb>
                    <BreadcrumbList>
                        <BreadcrumbItem>
                            <BreadcrumbPage><Link href={"/dashboard"}>Dashboard</Link></BreadcrumbPage>
                        </BreadcrumbItem>
                        <BreadcrumbSeparator />
                        <BreadcrumbItem>
                            <BreadcrumbPage>Moje Pola</BreadcrumbPage>
                        </BreadcrumbItem>
                    </BreadcrumbList>
                </Breadcrumb>
            </header>

            <div className="flex flex-1 flex-col gap-4 p-4 md:gap-8 md:p-8">
                <div className="flex items-center justify-between">
                    <div>
                        <h1 className="text-3xl font-bold tracking-tight">Moje Pola</h1>
                        <p className="text-muted-foreground mt-1 flex items-center gap-2">
                            Zarządzaj swoimi uprawami.
                            <Badge variant="outline" className="text-green-600 border-green-200 bg-green-50">
                                Łącznie: {totalArea.toFixed(2)} ha
                            </Badge>
                        </p>
                    </div>

                    <Dialog open={isOpen} onOpenChange={setIsOpen}>
                        <DialogTrigger asChild>
                            <Button className="bg-green-600 hover:bg-green-700">
                                <Plus className="mr-2 h-4 w-4" /> Dodaj pole
                            </Button>
                        </DialogTrigger>
                        <DialogContent className="max-w-[600px] max-h-[90vh] overflow-y-auto">
                            <DialogHeader>
                                <DialogTitle>Dodaj nowe pole</DialogTitle>
                                <DialogDescription>
                                    Uzupełnij informacje o polu i przypisz działki ewidencyjne.
                                </DialogDescription>
                            </DialogHeader>
                            <div className="grid gap-6 py-4">

                                <div className="grid grid-cols-2 gap-4">
                                    <div className="grid gap-2 col-span-2 md:col-span-1">
                                        <Label htmlFor="name">Nazwa pola <span className="text-red-500">*</span></Label>
                                        <Input
                                            id="name"
                                            placeholder="np. Za stodołą"
                                            value={form.name}
                                            onChange={e => setForm({...form, name: e.target.value})}
                                        />
                                    </div>
                                    <div className="grid gap-2 col-span-2 md:col-span-1">
                                        <Label htmlFor="area">Powierzchnia (ha) <span className="text-red-500">*</span></Label>
                                        <div className="relative">
                                            <Input
                                                id="area"
                                                type="number"
                                                min="0.01"
                                                step="0.01"
                                                placeholder="0.00"
                                                className="pl-8"
                                                value={form.area}
                                                onChange={e => setForm({...form, area: e.target.value})}
                                            />
                                            <Ruler className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                                        </div>
                                    </div>
                                </div>
                                <div className="grid gap-2">
                                    <Label htmlFor="desc">Opis</Label>
                                    <Textarea
                                        id="desc"
                                        placeholder="np. Pole za stodołą, staw przy granicy"
                                        value={form.description}
                                        onChange={e => setForm({...form, description: e.target.value})}
                                    />
                                </div>
                                <Separator />
                                <div className="space-y-4">
                                    <div className="space-y-1">
                                        <Label className="text-base font-semibold flex items-center gap-2">
                                            <MapPin className="h-4 w-4 text-blue-600" />
                                            Lokalizacja działek <span className="text-red-500">*</span>
                                        </Label>
                                        <p className="text-xs text-muted-foreground">
                                            Wybierz obręb i wpisz numer działki. Musisz dodać przynajmniej jedną.
                                        </p>
                                    </div>
                                    <TerytParcelSelector onParcelSelected={handleAddParcel} />

                                    {selectedParcels.length > 0 ? (
                                        <div className="bg-slate-50 p-4 rounded-lg border border-slate-200">
                                            <div className="flex flex-wrap gap-2">
                                                {selectedParcels.map(p => (
                                                    <Badge key={p} variant="secondary" className="bg-white border-blue-200 text-slate-700 cursor-pointer pr-1 group">
                                                        <span className="font-mono">{p}</span>
                                                        <button onClick={(e) => { e.stopPropagation(); handleRemoveParcel(p); }} className="ml-2 hover:text-red-500">×</button>
                                                    </Badge>
                                                ))}
                                            </div>
                                        </div>
                                    ) : (
                                        <div className="text-center py-4 text-xs text-red-400 border border-dashed border-red-200 rounded-lg bg-red-50/10">
                                            Lista działek jest pusta. Dodaj działkę powyżej.
                                        </div>
                                    )}
                                </div>
                            </div>

                            <Button
                                onClick={handleSubmit}
                                disabled={!isFormValid || isSubmitting}
                                className="w-full bg-green-600 hover:bg-green-700"
                            >
                                {isSubmitting ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : "Zapisz pole"}
                            </Button>
                        </DialogContent>
                    </Dialog>
                </div>

                {isLoading ? (
                    <div className="flex h-[400px] items-center justify-center">
                        <Loader2 className="h-8 w-8 animate-spin text-green-600" />
                    </div>
                ) : fields.length === 0 ? (
                    <div className="flex h-[400px] flex-col items-center justify-center gap-4 rounded-lg border border-dashed bg-slate-50/50 p-8 text-center">
                        <div className="rounded-full bg-green-100 p-4">
                            <Sprout className="h-8 w-8 text-green-600" />
                        </div>
                        <h3 className="text-lg font-semibold">Brak pól</h3>
                        <p className="text-sm text-muted-foreground max-w-sm">Dodaj swoje pierwsze pole uprawne.</p>
                        <Button variant="outline" onClick={() => setIsOpen(true)}>Dodaj pole</Button>
                    </div>
                ) : (
                    <div className="rounded-md border bg-white">
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead className="w-[250px]">Nazwa pola</TableHead>
                                    <TableHead>Powierzchnia</TableHead>
                                    <TableHead>Numery działek</TableHead>
                                    <TableHead className="hidden md:table-cell">Opis</TableHead>
                                    <TableHead className="text-right">Akcje</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {fields.map((field) => (
                                    <TableRow key={field.id}>
                                        <TableCell className="font-medium text-base">
                                            {field.name}
                                        </TableCell>
                                        <TableCell>
                                            <Badge variant="outline" className="font-bold border-green-200 text-green-700 bg-green-50">
                                                {field.area.toFixed(2)} ha
                                            </Badge>
                                        </TableCell>
                                        <TableCell>
                                            <div className="flex flex-wrap gap-1">
                                                {field.parcelNumbers && field.parcelNumbers.length > 0 ? (
                                                    <>
                                                        {field.parcelNumbers.slice(0, 3).map(pn => (
                                                            <Badge key={pn} variant="secondary" className="text-[10px] bg-slate-100 text-slate-600">
                                                                {pn}
                                                            </Badge>
                                                        ))}
                                                        {field.parcelNumbers.length > 3 && (
                                                            <Badge variant="outline" className="text-[10px] text-muted-foreground border-dashed">
                                                                +{field.parcelNumbers.length - 3}
                                                            </Badge>
                                                        )}
                                                    </>
                                                ) : (
                                                    <span className="text-xs text-muted-foreground">-</span>
                                                )}
                                            </div>
                                        </TableCell>
                                        <TableCell className="hidden md:table-cell text-muted-foreground text-sm">
                                            {field.description || "-"}
                                        </TableCell>
                                        <TableCell className="text-right">
                                            <DropdownMenu>
                                                <DropdownMenuTrigger asChild>
                                                    <Button variant="ghost" className="h-8 w-8 p-0">
                                                        <span className="sr-only">Otwórz menu</span>
                                                        <MoreHorizontal className="h-4 w-4" />
                                                    </Button>
                                                </DropdownMenuTrigger>
                                                <DropdownMenuContent align="end">
                                                    <DropdownMenuLabel>Akcje</DropdownMenuLabel>
                                                    <DropdownMenuItem onClick={() => navigator.clipboard.writeText(String(field.id))}>
                                                        Kopiuj ID
                                                    </DropdownMenuItem>
                                                    <DropdownMenuSeparator />
                                                    <Link href={`/dashboard/fields/${field.id}`} className="cursor-pointer">
                                                        <DropdownMenuItem className="cursor-pointer">
                                                            <ArrowRight className="mr-2 h-4 w-4" /> Szczegóły
                                                        </DropdownMenuItem>
                                                    </Link>
                                                    <DropdownMenuItem
                                                        onClick={() => handleDelete(field.id, field.name)}
                                                        className="text-red-600 focus:text-red-600"
                                                    >
                                                        <Trash2 className="mr-2 h-4 w-4" /> Usuń
                                                    </DropdownMenuItem>
                                                </DropdownMenuContent>
                                            </DropdownMenu>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </div>
                )}
            </div>
        </>
    );
}