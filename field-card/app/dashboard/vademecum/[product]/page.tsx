import { AppSidebar } from "@/components/app-sidebar"
import {
    Breadcrumb,
    BreadcrumbItem,
    BreadcrumbList,
    BreadcrumbPage,
} from "@/components/ui/breadcrumb"
import { Separator } from "@/components/ui/separator"
import {
    SidebarInset,
    SidebarProvider,
    SidebarTrigger,
} from "@/components/ui/sidebar"

interface PageProps {
    params: Promise<{
        product: string;
    }>;
}

async function getProductData(id: string) {
    try {

        const res = await fetch(`http://localhost:8080/api/products/${id}`, {
            cache: "no-store",
        });

        if (!res.ok) {
            console.error("Błąd fetch:", res.status, res.statusText);
            return null;
        }
        return res.json();
    } catch (error) {
        console.error("Błąd sieci:", error);
        return null;
    }
}

export default async function Page({ params }: PageProps) {
    const resolvedParams = await params;

    const id = resolvedParams.product;

    const productData = await getProductData(id);

    return (
        <SidebarProvider>
            <AppSidebar />
            <SidebarInset>
                <header className="flex h-16 shrink-0 items-center gap-2 px-4">
                    <SidebarTrigger className="-ml-1" />
                    <Separator orientation="vertical" className="mr-2 h-4" />
                    <Breadcrumb>
                        <BreadcrumbList>
                            <BreadcrumbItem>
                                <BreadcrumbPage>Szczegóły produktu</BreadcrumbPage>
                            </BreadcrumbItem>
                        </BreadcrumbList>
                    </Breadcrumb>
                </header>

                <div className="flex flex-1 flex-col gap-4 p-4 pt-0">
                    <div className="rounded-lg border bg-card text-card-foreground shadow-sm p-6">
                        <h2 className="text-xl font-semibold mb-4">
                            ID Bazy Danych: <span className="font-mono text-blue-600">{id}</span>
                        </h2>

                        <h3 className="text-sm font-medium text-muted-foreground mb-2">Odpowiedź z serwera (JSON):</h3>

                        {productData ? (
                            <pre className="bg-slate-950 text-slate-50 p-4 rounded-md overflow-auto font-mono text-sm max-h-[600px]">
                                {JSON.stringify(productData, null, 2)}
                            </pre>
                        ) : (
                            <div className="p-4 border border-red-200 bg-red-50 text-red-700 rounded-md">
                                <p className="font-bold">Nie udało się pobrać danych.</p>
                                <p className="text-sm">Diagnoza:</p>
                                <ul className="list-disc list-inside text-sm ml-2 mt-1">
                                    <li>Szukane ID: "{id}"</li>
                                    <li>Czy endpoint @GetMapping("/&#123;id&#125;") w Springu jest gotowy?</li>
                                    <li>Czy produkt o ID {id} (nie mylić z sorId) istnieje w tabeli?</li>
                                </ul>
                            </div>
                        )}
                    </div>
                </div>
            </SidebarInset>
        </SidebarProvider>
    )
}