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

    const createUrl = (newPage: number | string) => {
        const params = new URLSearchParams(searchParams.toString())
        params.set("page", newPage.toString())
        return `${pathname}?${params.toString()}`
    }

    return (
        <div className="space-y-6">
            <SearchForm
                defaultValues={currentFilters}
                onSearch={handleSearch}
            />

            {isLoading && <div className="text-muted-foreground animate-pulse text-center py-8">Pobieranie danych... 🍃</div>}

            {isError && <div className="text-red-500 font-medium text-center py-8">Nie udało się pobrać produktów. Sprawdź połączenie.</div>}

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

            {data && data.totalPages > 1 && (
                <Pagination>
                    <PaginationContent>
                        <PaginationItem>
                            <PaginationPrevious
                                href={createUrl((currentFilters.page || 0) - 1)}
                                aria-disabled={(currentFilters.page || 0) === 0}
                                className={(currentFilters.page || 0) === 0 ? 'pointer-events-none opacity-50' : ''}
                            />
                        </PaginationItem>

                        <PaginationItem>
                            <PaginationLink href={createUrl(currentFilters.page || 0)} isActive>
                                {(currentFilters.page || 0) + 1}
                            </PaginationLink>
                        </PaginationItem>

                        {data.totalPages > 5 && <PaginationItem><PaginationEllipsis /></PaginationItem>}

                        <PaginationItem>
                            <PaginationNext
                                href={createUrl((currentFilters.page || 0) + 1)}
                                aria-disabled={(currentFilters.page || 0) >= data.totalPages - 1}
                                className={(currentFilters.page || 0) >= data.totalPages - 1 ? 'pointer-events-none opacity-50' : ''}
                            />
                        </PaginationItem>
                    </PaginationContent>
                </Pagination>
            )}
        </div>
    )
}