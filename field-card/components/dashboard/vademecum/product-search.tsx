"use client"

import { useRouter, usePathname, useSearchParams } from "next/navigation";
import { useProductSearch } from "@/hooks/useProductSearch"
import SearchForm from "@/components/dashboard/vademecum/search-form";
import {
    Pagination,
    PaginationContent,
    PaginationEllipsis,
    PaginationItem,
    PaginationLink,
    PaginationNext,
    PaginationPrevious,
} from "@/components/ui/pagination"
import { SearchCriteriaSchema, type SearchCriteria } from "@/lib/schemas/product";
import { ProductCard } from "@/components/dashboard/vademecum/product-card";
import {Loader2} from "lucide-react";

export function ProductSearch() {
    const searchParams = useSearchParams()
    const pathname = usePathname()
    const { replace } = useRouter()

    const rawParams = {
        page: searchParams.get('page'),
        query: searchParams.get('query'),
        cropName: searchParams.getAll('cropName'),
        pestName: searchParams.getAll('pestName'),
        activeSubstance: searchParams.getAll('activeSubstance'),
        productType: searchParams.getAll('productType'),
    };

    const currentFilters = SearchCriteriaSchema.parse(rawParams);

    const { data, isLoading, isError } = useProductSearch({
        ...currentFilters,
        page: currentFilters.page,
    })

    const handleSearch = (filters: Omit<SearchCriteria, 'page' | 'size'>) => {
        const params = new URLSearchParams(searchParams.toString())
        params.set("page", "0")

        const updateArrayParam = (key: string, values: string[] | undefined) => {
            params.delete(key)
            if (values && values.length > 0) {
                values.forEach(v => params.append(key, v))
            }
        }

        if (filters.query) {
            params.set("query", filters.query)
        } else {
            params.delete("query")
        }

        updateArrayParam("cropName", filters.cropName)
        updateArrayParam("pestName", filters.pestName)
        updateArrayParam("activeSubstance", filters.activeSubstance)
        updateArrayParam("productType", filters.productType)

        replace(`${pathname}?${params.toString()}`)
    }
    const totalResults = data?.totalElements || 0;
    const createUrl = (newPage: number | string) => {
        const params = new URLSearchParams(searchParams.toString())
        params.set("page", newPage.toString())
        return `${pathname}?${params.toString()}`
    }
    const currentPage = currentFilters.page || 0; // index 0-based
    const totalPages = data?.totalPages || 0;

    const getPageNumbers = () => {
        const pages = [];
        const showMax = 5;

        if (totalPages <= showMax) {
            for (let i = 0; i < totalPages; i++) {
                pages.push(i);
            }
            return pages;
        }


        pages.push(0);

        let start = Math.max(1, currentPage - 1);
        let end = Math.min(totalPages - 2, currentPage + 1);

        if (currentPage < 3) {
            end = Math.min(totalPages - 2, 3);
        }

        if (currentPage > totalPages - 4) {
            start = Math.max(1, totalPages - 4);
        }
        if (start > 1) {
            pages.push("ellipsis-start");
        }

        for (let i = start; i <= end; i++) {
            pages.push(i);
        }
        if (end < totalPages - 2) {
            pages.push("ellipsis-end");
        }
        pages.push(totalPages - 1);

        return pages;
    };

    return (
        <div className="space-y-6">
            <SearchForm
                defaultValues={currentFilters}
                onSearch={handleSearch}
            />

            {isLoading && <div className="flex items-center justify-center h-screen">
                <Loader2 className="h-8 w-8 animate-spin text-primary" />
            </div>}

            {isError && <div className="text-red-500 font-medium text-center py-8">Nie udało się pobrać produktów. Sprawdź połączenie.</div>}
            {!isLoading && (
                <div className="text-muted-foreground text-sm mb-4">
                    Znaleziono wyników: <span className="font-bold text-foreground">{totalResults}</span>
                </div>
            )}
            {!isLoading && (data?.suggestions?.length ?? 0) > 0 && (
                <p className="text-center text-muted-foreground py-4">Czy miałeś na myśli?</p>
            )}

            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
                {[...(data?.results || []), ...(data?.suggestions || [])].map((product) => (
                    <ProductCard
                        key={product.sorId || product.id}
                        product={{
                            id: String(product.id),
                            name: product.name,
                            manufacturer: product.manufacturer,
                            type: Array.isArray(product.type) ? product.type : [product.type || "Inny"],
                            activeSubstances: Array.isArray(product.activeSubstance) ? product.activeSubstance : [product.activeSubstance],
                            crops: product.crops || [],
                            pests: product.pests || []
                        }}
                    />
                ))}
            </div>

            {!isLoading && data?.results?.length === 0 && data?.suggestions?.length === 0 && (
                <div className="text-center py-12 bg-muted/30 rounded-lg border border-dashed">
                    <p className="text-muted-foreground">Nie znaleziono produktów pasujących do zapytania.</p>
                </div>
            )}

            {data && totalPages > 1 && (
                <Pagination className="mt-4">
                    <PaginationContent>
                        {/* PRZYCISK WSTECZ */}
                        <PaginationItem>
                            <PaginationPrevious
                                href={currentPage > 0 ? createUrl(currentPage - 1) : "#"}
                                aria-disabled={currentPage === 0}
                                className={currentPage === 0 ? "pointer-events-none opacity-50" : ""}
                            />
                        </PaginationItem>

                        {/* NUMERY STRON + KROPKI */}
                        {getPageNumbers().map((page, index) => {
                            if (page === "ellipsis-start" || page === "ellipsis-end") {
                                return (
                                    <PaginationItem key={page}>
                                        <PaginationEllipsis />
                                    </PaginationItem>
                                );
                            }

                            return (
                                <PaginationItem key={index}>
                                    <PaginationLink
                                        href={createUrl(page as number)}
                                        isActive={currentPage === page}
                                    >
                                        {/* Wyświetlamy +1, bo dla ludzi strony są od 1, a dla backendu od 0 */}
                                        {(page as number) + 1}
                                    </PaginationLink>
                                </PaginationItem>
                            );
                        })}

                        {/* PRZYCISK DALEJ */}
                        <PaginationItem>
                            <PaginationNext
                                href={currentPage < totalPages - 1 ? createUrl(currentPage + 1) : "#"}
                                aria-disabled={currentPage >= totalPages - 1}
                                className={currentPage >= totalPages - 1 ? "pointer-events-none opacity-50" : ""}
                            />
                        </PaginationItem>
                    </PaginationContent>
                </Pagination>
            )}
        </div>
    )
}