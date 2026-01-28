import { AppSidebar } from "@/components/app-sidebar"
import { cn } from "@/lib/utils";
import {
    Breadcrumb,
    BreadcrumbItem,
    BreadcrumbList,
    BreadcrumbPage,
    BreadcrumbLink,
    BreadcrumbSeparator
} from "@/components/ui/breadcrumb"
import { Separator } from "@/components/ui/separator"
import {
    SidebarInset,
    SidebarProvider,
    SidebarTrigger,
} from "@/components/ui/sidebar"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import {
    FlaskConical,
    Factory,
    Calendar,
    FileText,
    Leaf,
    Bug,
    AlertTriangle,
    ArrowLeft,
    CheckCircle2, AlertCircle
} from "lucide-react"
import Link from "next/link"
import { productService } from "@/app/api/products/product-service"
import {
    Tooltip,
    TooltipContent,
    TooltipTrigger,
} from "@/components/ui/tooltip"
import {BackButton} from "@/components/back-button";

interface PageProps {
    params: Promise<{
        product: string;
    }>;
}

export default async function Page({ params }: PageProps) {
    const resolvedParams = await params;
    const id = resolvedParams.product;

    const product = await productService.getProductById(id);

    const formatDate = (dateString?: string | null) => {
        if (!dateString) return "Bezterminowo";
        return new Date(dateString).toLocaleDateString("pl-PL", {
            year: "numeric", month: "long", day: "numeric"
        });
    }
    const isExpired = (dateString : string | null | undefined) => {
        if (!dateString) return false;
        const date = new Date(dateString);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        return date < today;
    };

    return (
        <>
                <header className="flex h-16 shrink-0 items-center gap-2 px-4 border-b bg-background sticky top-0 z-10">
                    <SidebarTrigger className="-ml-1" />
                    <Separator orientation="vertical" className="mr-2 h-4" />
                    <Breadcrumb>
                        <BreadcrumbList>
                            <BreadcrumbItem>
                                <BreadcrumbLink href="/dashboard">Dashboard</BreadcrumbLink>
                            </BreadcrumbItem>
                            <BreadcrumbSeparator />
                            <BreadcrumbItem>
                                <BreadcrumbLink href="/dashboard/vademecum">Vademecum</BreadcrumbLink>
                            </BreadcrumbItem>
                            <BreadcrumbSeparator />
                            <BreadcrumbItem>
                                <BreadcrumbPage>{product?.name || "Szczegóły produktu"}</BreadcrumbPage>
                            </BreadcrumbItem>
                        </BreadcrumbList>
                    </Breadcrumb>
                </header>

                <div className="flex flex-1 flex-col gap-6 p-4 md:p-8 max-w-7xl mx-auto w-full">
                    {!product ? (
                        <div className="flex flex-col items-center justify-center h-[50vh] gap-4 text-center">
                            <AlertTriangle className="h-16 w-16 text-red-500 opacity-20" />
                            <h2 className="text-2xl font-bold text-red-600">Nie znaleziono danych</h2>
                            <p className="text-muted-foreground">Nie udało się pobrać szczegółów dla ID: {id}</p>
                            <Button asChild variant="outline">
                                <Link href="/dashboard/vademecum"><ArrowLeft className="mr-2 h-4 w-4"/> Wróć do listy</Link>
                            </Button>
                        </div>
                    ) : (
                        <>

                            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                                <div>

                                    <div className="flex flex-wrap items-center gap-2 mb-2">
                                        <BackButton />

                                        {product.type.map((t, i) => (
                                            <Badge key={i} variant="default" className="bg-blue-100 text-blue-800 hover:bg-blue-200">
                                                {t}
                                            </Badge>
                                        ))}
                                        <Badge
                                            variant="outline"
                                            className={cn(
                                                "flex w-fit items-center gap-1 transition-colors",
                                                isExpired(product.salesDeadline)
                                                    ? "border-destructive text-destructive bg-destructive/10"
                                                    : "border-green-600 text-green-700 bg-green-50"
                                            )}
                                        >
                                            {isExpired(product?.salesDeadline) ? (
                                                <>
                                                    <AlertCircle className="w-3 h-3" />
                                                    <span>Wycofany!</span>
                                                </>
                                            ) : (
                                                <>
                                                    <CheckCircle2 className="w-3 h-3" />
                                                    <span>Dopuszczony do obrotu</span>
                                                </>
                                            )}
                                        </Badge>
                                    </div>
                                    <h1 className="text-3xl md:text-4xl font-extrabold tracking-tight text-primary">
                                        {product.name}
                                    </h1>
                                    <div className="flex items-center gap-2 mt-2 text-muted-foreground">
                                        <Factory className="h-4 w-4" />
                                        <span className="font-medium">{product.manufacturer}</span>
                                    </div>
                                </div>

                                <div className="flex flex-col gap-2 md:items-end">
                                    {product.labelUrl && (
                                        <Button asChild variant="default" size="sm">
                                            <a href={product.labelUrl} target="_blank" rel="noopener noreferrer">
                                                <FileText className="mr-2 h-4 w-4" /> Pobierz Etykietę
                                            </a>
                                        </Button>
                                    )}
                                    <div className="text-sm text-right text-muted-foreground">
                                        Nr zezwolenia: <span className="font-mono font-bold text-foreground">{product.permitNumber}</span>
                                    </div>
                                </div>
                            </div>

                            <Separator />

                            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">

                                <Card className="md:col-span-2 shadow-sm border-l-4 border-l-indigo-500">
                                    <CardHeader className="pb-2">
                                        <CardTitle className="flex items-center gap-2 text-lg">
                                            <FlaskConical className="h-5 w-5 text-indigo-500" />
                                            Substancje czynne
                                        </CardTitle>
                                    </CardHeader>
                                    <CardContent>
                                        <ul className="space-y-2">
                                            {product.activeSubstances.length > 0 ? (
                                                product.activeSubstances.map((sub, i) => (
                                                    <li key={i} className="flex items-center gap-2 bg-indigo-50/50 p-2 rounded border border-indigo-100 text-indigo-900 font-medium">
                                                        <div className="h-2 w-2 rounded-full bg-indigo-500" />
                                                        {sub}
                                                    </li>
                                                ))
                                            ) : (
                                                <p className="text-muted-foreground italic">Brak danych o składzie</p>
                                            )}
                                        </ul>
                                    </CardContent>
                                </Card>

                                <Card className="shadow-sm">
                                    <CardHeader className="pb-2">
                                        <CardTitle className="flex items-center gap-2 text-lg">
                                            <Calendar className="h-5 w-5 text-orange-500" />
                                            Terminy ważności
                                        </CardTitle>
                                    </CardHeader>
                                    <CardContent className="space-y-4 pt-4">
                                        <div>
                                            <p className="text-xs text-muted-foreground uppercase font-semibold">Do stosowania:</p>

                                            <div className="flex items-center gap-2">
                                                <p className={cn(
                                                    "text-lg font-bold",
                                                    isExpired(product.useDeadline) ? "text-destructive" : "text-foreground"
                                                )}>
                                                    {formatDate(product.useDeadline)}
                                                </p>

                                                {isExpired(product.useDeadline) && (
                                                    <Tooltip>
                                                        <TooltipTrigger><AlertCircle className="w-4 h-4 mr-1 text-destructive" /></TooltipTrigger>
                                                        <TooltipContent>
                                                            <p>Termin użycia minął!</p>
                                                        </TooltipContent>
                                                    </Tooltip>
                                                )}
                                            </div>
                                        </div>

                                        {product.salesDeadline && (
                                            <div>
                                                <p className="text-xs text-muted-foreground uppercase font-semibold">Do sprzedaży:</p>
                                                <div className="flex items-center gap-2">
                                                    <p className={cn(
                                                        "text-lg font-bold",
                                                        isExpired(product.salesDeadline) ? "text-destructive" : "text-foreground"
                                                    )}>
                                                        {formatDate(product.salesDeadline)}
                                                    </p>
                                                    {isExpired(product.salesDeadline) && (
                                                        <Tooltip>
                                                            <TooltipTrigger><AlertCircle className="w-4 h-4 mr-1 text-destructive" /></TooltipTrigger>
                                                            <TooltipContent>
                                                                <p>Termin sprzedaży minął!</p>
                                                            </TooltipContent>
                                                        </Tooltip>
                                                    )}
                                                </div>
                                            </div>
                                        )}
                                    </CardContent>
                                </Card>

                                <Card className="md:col-span-3 shadow-sm">
                                    <CardHeader>
                                        <CardTitle>Zakres stosowania</CardTitle>
                                        <CardDescription>Szybki podgląd zarejestrowanych roślin i zwalczanych organizmów</CardDescription>
                                    </CardHeader>
                                    <CardContent className="grid md:grid-cols-2 gap-8">
                                        <div className={'border-r border-b-muted/50 mr-2'}>
                                            <h4 className="flex items-center gap-2 text-sm font-semibold mb-3 text-emerald-700">
                                                <Leaf className="h-4 w-4" /> Uprawy
                                            </h4>
                                            <div className="flex flex-wrap gap-2">
                                                {product.crops.length > 0 ? product.crops.map((crop, i) => (
                                                    <Badge key={i} variant="outline" className="border-emerald-200 bg-emerald-50 text-emerald-800">
                                                        {crop}
                                                    </Badge>
                                                )) : <span className="text-muted-foreground text-sm">Brak danych</span>}
                                            </div>
                                        </div>
                                        <div >
                                            <h4 className="flex items-center gap-2 text-sm font-semibold mb-3 text-rose-700">
                                                <Bug className="h-4 w-4" /> Agrofagi
                                            </h4>
                                            <div className="flex flex-wrap gap-2">
                                                {product.pests.length > 0 ? product.pests.map((pest, i) => (
                                                    <Badge key={i} variant="outline" className="border-rose-200 bg-rose-50 text-rose-800">
                                                        {pest}
                                                    </Badge>
                                                )) : <span className="text-muted-foreground text-sm">Brak danych</span>}
                                            </div>
                                        </div>
                                    </CardContent>
                                </Card>

                                <Card className="md:col-span-3 shadow-sm">
                                    <CardHeader>
                                        <CardTitle>Szczegółowe dawkowanie</CardTitle>
                                        <CardDescription>Zalecenia stosowania według etykiety rejestracyjnej</CardDescription>
                                    </CardHeader>
                                    <CardContent>
                                        {product.usages.length > 0 ? (
                                            <div className="rounded-md border">
                                                <Table>
                                                    <TableHeader>
                                                        <TableRow className="bg-muted/50">
                                                            <TableHead className="w-[200px]">Uprawa</TableHead>
                                                            <TableHead className="w-[200px]">Agrofag</TableHead>
                                                            <TableHead>Dawka i uwagi</TableHead>
                                                        </TableRow>
                                                    </TableHeader>
                                                    <TableBody>
                                                        {product.usages.map((usage, index) => (
                                                            <TableRow key={index}>
                                                                <TableCell className="font-medium">{usage.cropName}</TableCell>
                                                                <TableCell>{usage.pestName}</TableCell>
                                                                <TableCell className="whitespace-pre-line text-sm text-muted-foreground">
                                                                    {usage.dose}
                                                                </TableCell>
                                                            </TableRow>
                                                        ))}
                                                    </TableBody>
                                                </Table>
                                            </div>
                                        ) : (
                                            <div className="text-center py-8 text-muted-foreground">
                                                Brak szczegółowej tabeli dawkowania dla tego produktu.
                                            </div>
                                        )}
                                    </CardContent>
                                </Card>
                            </div>

                            <div className="mt-8 pt-4 border-t text-xs text-muted-foreground flex justify-between">
                                <span>Ostatnia aktualizacja danych: {new Date().toLocaleDateString()}</span>
                            </div>
                        </>
                    )}
                </div>
        </>
    )
}