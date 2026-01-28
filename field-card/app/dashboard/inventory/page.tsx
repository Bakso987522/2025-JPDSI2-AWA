"use client";

import { useState } from "react";
import { useInventory } from "@/hooks/useInventory";
import { Button } from "@/components/ui/button";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";
import { Plus, Trash2, AlertTriangle, Loader2, Package } from "lucide-react";
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useProductSearch } from "@/hooks/useProductSearch";
import { useDebounce } from "@/hooks/useDebounce";
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
import {
    Tooltip,
    TooltipContent,
    TooltipTrigger,
} from "@/components/ui/tooltip";
import { toast } from "sonner";

import { z } from "zod";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";

const inventorySchema = z.object({
    quantity: z.coerce.number() // coerce zamienia string z inputa na number
        .min(0.01, "Ilość musi być większa od 0")
        .max(99999, "Zbyt duża ilość"),
    unit: z.enum(["L", "KG", "SZT"]),
    batchNumber: z.string().optional(),
    expirationDate: z.string().optional()
        .refine((val) => !val || new Date(val).toString() !== 'Invalid Date', {
            message: "Nieprawidłowa data"
        }),
});

type InventoryFormValues = z.infer<typeof inventorySchema>;

export default function InventoryPage() {
    const {
        items: stock,
        isLoading,
        deleteItem,
        addItem,
        isAdding,
        isDeleting
    } = useInventory();

    const [isDialogOpen, setIsDialogOpen] = useState(false);

    const [searchQuery, setSearchQuery] = useState("");
    const debouncedQuery = useDebounce(searchQuery, 500);
    const { data: searchResults } = useProductSearch({ query: debouncedQuery, page: 0, size: 10 });
    const [selectedProduct, setSelectedProduct] = useState<{ id: number, name: string } | null>(null);

    const form = useForm({
        resolver: zodResolver(inventorySchema),
        defaultValues: {
            quantity: 0,
            unit: "L",
            batchNumber: "",
            expirationDate: "",
        },
    });

    const onSubmit = async (data: InventoryFormValues) => {
        if (!selectedProduct) {
            toast.error("Musisz wybrać produkt z listy!");
            return;
        }

        try {
            await addItem({
                productId: selectedProduct.id,
                quantity: data.quantity,
                unit: data.unit,
                batchNumber: data.batchNumber || undefined,
                expirationDate: data.expirationDate || undefined
            });

            toast.success(`Dodano ${selectedProduct.name} do magazynu`);
            setIsDialogOpen(false);
            form.reset();
            setSelectedProduct(null);
            setSearchQuery("");

        } catch (error) {
            toast.error("Nie udało się dodać produktu. Spróbuj ponownie.");
            console.error(error);
        }
    };

    const handleDelete = async (id: number, name: string) => {
        try {
            await deleteItem(id);
            toast.success(`Usunięto ${name} z magazynu`);
        } catch (error) {
            toast.error("Nie udało się usunąć elementu.");
        }
    }

    return (
        <>
            <header className="flex h-16 shrink-0 items-center gap-2 px-4">
                <SidebarTrigger className="-ml-1" />
                <Separator orientation="vertical" className="mr-2 h-4" />
                <Breadcrumb>
                    <BreadcrumbList>
                        <BreadcrumbItem>
                            <BreadcrumbPage><Link href={"/dashboard"}>Dashboard</Link></BreadcrumbPage>
                        </BreadcrumbItem>
                        <BreadcrumbSeparator />
                        <BreadcrumbItem>
                            <BreadcrumbPage>Inwentaryzacja</BreadcrumbPage>
                        </BreadcrumbItem>
                    </BreadcrumbList>
                </Breadcrumb>
            </header>

            <div className="space-y-6 p-4 md:p-8">
                <div className="flex items-center justify-between">
                    <div>
                        <h1 className="text-3xl font-bold tracking-tight">Magazyn ŚOR</h1>
                        <p className="text-muted-foreground">
                            Ewidencja środków ochrony roślin w Twoim gospodarstwie.
                        </p>
                    </div>

                    <Dialog open={isDialogOpen} onOpenChange={(open) => {
                        setIsDialogOpen(open);
                        if (!open) form.reset();
                    }}>
                        <DialogTrigger asChild>
                            <Button><Plus className="mr-2 h-4 w-4" /> Dodaj środek</Button>
                        </DialogTrigger>
                        <DialogContent className="sm:max-w-[500px]">
                            <DialogHeader>
                                <DialogTitle>Przyjęcie na magazyn</DialogTitle>
                            </DialogHeader>

                            <div className="grid gap-2 py-2">
                                <Label>Wyszukaj produkt <span className="text-red-500">*</span></Label>
                                {!selectedProduct && (
                                    <Input
                                        placeholder="Wpisz nazwę środka..."
                                        value={searchQuery}
                                        onChange={(e) => setSearchQuery(e.target.value)}
                                    />
                                )}
                                {selectedProduct && (
                                    <div className="bg-green-50 p-2 rounded text-sm text-green-700 flex justify-between items-center border border-green-200">
                                        <span>Wybrano: <b>{selectedProduct.name}</b></span>
                                        <Button variant="ghost" size="sm" onClick={() => setSelectedProduct(null)} className="hover:text-green-900">Zmień</Button>
                                    </div>
                                )}

                                {!selectedProduct && (searchResults?.results?.length ?? 0) > 0 && (
                                    <div className="border rounded-md max-h-[150px] overflow-y-auto bg-white shadow-sm z-10">
                                        {searchResults?.results.map(p => (
                                            <div
                                                key={p.id}
                                                className="p-2 hover:bg-slate-100 cursor-pointer text-sm border-b last:border-0"
                                                onClick={() => {
                                                    setSelectedProduct({ id: p.id, name: p.name });
                                                    setSearchQuery("");
                                                }}
                                            >
                                                {p.name} <span className="text-gray-400">({p.manufacturer})</span>
                                            </div>
                                        ))}
                                    </div>
                                )}
                                {!selectedProduct && (searchResults?.suggestions?.length ?? 0) > 0 && (
                                    <div className="border rounded-md max-h-[150px] overflow-y-auto bg-white shadow-sm z-10">
                                        {searchResults?.suggestions.map(p => (
                                            <div
                                                key={p.id}
                                                className="p-2 hover:bg-slate-100 cursor-pointer text-sm border-b last:border-0"
                                                onClick={() => {
                                                    setSelectedProduct({ id: p.id, name: p.name });
                                                    setSearchQuery("");
                                                }}
                                            >
                                                {p.name} <span className="text-gray-400">({p.manufacturer})</span>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>

                            <Form {...form}>
                                <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">

                                    <div className="grid grid-cols-2 gap-4">
                                        <FormField
                                            control={form.control}
                                            name="quantity"
                                            render={({ field }) => (
                                                <FormItem>
                                                    <FormLabel>Ilość <span className="text-red-500">*</span></FormLabel>
                                                    <FormControl>
                                                        <Input type="number" step="0.01" {...field} value={field.value as string | number}  />
                                                    </FormControl>
                                                    <FormMessage />
                                                </FormItem>
                                            )}
                                        />

                                        <FormField
                                            control={form.control}
                                            name="unit"
                                            render={({ field }) => (
                                                <FormItem>
                                                    <FormLabel>Jednostka</FormLabel>
                                                    <FormControl>
                                                        <select
                                                            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                                                            {...field}
                                                        >
                                                            <option value="L">Litr (L)</option>
                                                            <option value="KG">Kilogram (kg)</option>
                                                            <option value="SZT">Sztuka</option>
                                                        </select>
                                                    </FormControl>
                                                    <FormMessage />
                                                </FormItem>
                                            )}
                                        />
                                    </div>

                                    <FormField
                                        control={form.control}
                                        name="batchNumber"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormLabel>Numer partii</FormLabel>
                                                <FormControl>
                                                    <Input placeholder="Opcjonalne" {...field} />
                                                </FormControl>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />

                                    <FormField
                                        control={form.control}
                                        name="expirationDate"
                                        render={({ field }) => (
                                            <FormItem>
                                                <FormLabel>Data ważności</FormLabel>
                                                <FormControl>
                                                    <Input type="date" {...field} />
                                                </FormControl>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />

                                    <Button
                                        type="submit"
                                        disabled={isAdding}
                                        className="w-full"
                                    >
                                        {isAdding ? (
                                            <><Loader2 className="mr-2 h-4 w-4 animate-spin" /> Zapisywanie...</>
                                        ) : (
                                            "Zapisz w magazynie"
                                        )}
                                    </Button>
                                </form>
                            </Form>
                        </DialogContent>
                    </Dialog>
                </div>

                {isLoading ? (
                    <div className="flex items-center justify-center">
                        <Loader2 className="h-8 w-8 animate-spin text-primary" />
                    </div>
                ) : (
                    <div className="rounded-md border bg-white" >
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>Nazwa Produktu</TableHead>
                                    <TableHead>Producent</TableHead>
                                    <TableHead>Nr Partii</TableHead>
                                    <TableHead>Data Ważności</TableHead>
                                    <TableHead className="text-right">Stan</TableHead>
                                    <TableHead className="w-[50px]"></TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {stock?.length === 0 && (
                                    <TableRow>
                                        <TableCell colSpan={6} className="text-center py-12 text-muted-foreground flex flex-col items-center justify-center">
                                            <Package className="h-10 w-10 mb-2 opacity-20" />
                                            <p>Magazyn jest pusty.</p>
                                            <p className="text-sm">Dodaj pierwszy produkt używając przycisku powyżej.</p>
                                        </TableCell>
                                    </TableRow>
                                )}
                                {stock?.map((item) => (
                                    <TableRow key={item.id}>
                                        <TableCell className="font-medium">
                                            <Tooltip>
                                                <TooltipTrigger asChild>
                                                    <Link href={`/dashboard/vademecum/${item.productId}`} className="hover:underline hover:text-primary transition-colors">
                                                        {item.productName}
                                                    </Link>
                                                </TooltipTrigger>
                                                <TooltipContent>
                                                    <p>Przejdź do karty produktu</p>
                                                </TooltipContent>
                                            </Tooltip>
                                        </TableCell>
                                        <TableCell className="text-muted-foreground">{item.manufacturer}</TableCell>
                                        <TableCell>{item.batchNumber || "-"}</TableCell>
                                        <TableCell>
                                            {item.expirationDate ? (
                                                <div className="flex items-center gap-2">
                                                    {item.expirationDate}
                                                    {new Date(item.expirationDate) < new Date() && (
                                                        <Tooltip>
                                                            <TooltipTrigger>
                                                                <AlertTriangle className="h-4 w-4 text-red-500" />
                                                            </TooltipTrigger>
                                                            <TooltipContent>Produkt przeterminowany</TooltipContent>
                                                        </Tooltip>
                                                    )}
                                                </div>
                                            ) : "-"}
                                        </TableCell>
                                        <TableCell className="text-right font-bold text-slate-700">
                                            {item.quantity} <span className="text-xs font-normal text-muted-foreground">{item.unit}</span>
                                        </TableCell>
                                        <TableCell>
                                            <Button
                                                variant="ghost"
                                                size="icon"
                                                className="h-8 w-8 text-red-500 hover:text-red-700 hover:bg-red-50"
                                                disabled={isDeleting}
                                                onClick={() => handleDelete(item.id, item.productName)}
                                            >
                                                {isDeleting ? (
                                                    <Loader2 className="h-4 w-4 animate-spin" />
                                                ) : (
                                                    <Trash2 className="h-4 w-4" />
                                                )}
                                            </Button>
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