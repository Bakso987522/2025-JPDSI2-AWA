"use client"

import { useState } from "react"
import { useProductSearch } from "@/hooks/useProductSearch"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { useDebounce } from "@/hooks/useDebounce"
import SearchForm from "@/components/dashboard/vademecum/search-form";

export function ProductSearch() {
    const [query, setQuery] = useState("")
    const [page, setPage] = useState(0)

    const debouncedQuery = useDebounce(query, 500)

    const { data, isLoading, isError } = useProductSearch({
        query: debouncedQuery,
        page: page,
    })

    return (
        <div className="space-y-6">
            <SearchForm
                query={query}
                setQuery={setQuery}
                setPage={setPage}
            />

            {isLoading && <div className="text-muted-foreground animate-pulse">Pobieranie danych... 🍃</div>}

            {isError && <div className="text-red-500 font-medium">Nie udało się pobrać produktów. Sprawdź połączenie.</div>}
            {!isLoading && data?.suggestions.length > 0 && (
                <p className="text-center text-muted-foreground py-8">Czy miałeś na myśli?</p>
            )}

            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                {data?.results.map((product)  => (
                    <Card key={product.sorId} className="hover:shadow-md transition-shadow cursor-pointer">
                        <CardHeader className="pb-2">
                            <CardTitle className="text-lg text-primary">{product.name}</CardTitle>
                        </CardHeader>
                        <CardContent>
                            <p className="text-sm text-muted-foreground mb-1">
                                🏭 {product.manufacturer}
                            </p>
                            <p className="text-xs bg-muted/50 p-1 rounded">
                                🧪 {product.activeSubstance}
                            </p>
                        </CardContent>
                    </Card>
                ))}
                {data?.suggestions.map((product)  => (
                    <Card key={product.sorId} className="hover:shadow-md transition-shadow cursor-pointer">
                        <CardHeader className="pb-2">
                            <CardTitle className="text-lg text-primary">{product.name}</CardTitle>
                        </CardHeader>
                        <CardContent>
                            <p className="text-sm text-muted-foreground mb-1">
                                🏭 {product.manufacturer}
                            </p>
                            <p className="text-xs bg-muted/50 p-1 rounded">
                                🧪 {product.activeSubstance}
                            </p>
                        </CardContent>
                    </Card>
                ))}
            </div>

            {!isLoading && data?.results.length === 0 && data?.suggestions.length === 0 && (
                <p className="text-center text-muted-foreground py-8">Nie znaleziono produktów pasujących do zapytania.</p>
            )}


            {data && data.totalPages > 1 && (
                <div className="flex justify-center gap-2 mt-4 items-center">
                    <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setPage((p) => Math.max(0, p - 1))}
                        disabled={page === 0 || isLoading}
                    >
                        Poprzednia
                    </Button>
                    <span className="text-sm font-medium px-2">
            Strona {page + 1} z {data.totalPages}
          </span>
                    <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setPage((p) => p + 1)}
                        disabled={page >= data.totalPages - 1 || isLoading}
                    >
                        Następna
                    </Button>
                </div>
            )}
        </div>
    )
}