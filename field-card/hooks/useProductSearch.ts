import { useQuery } from "@tanstack/react-query";
import { SearchResponseSchema, type SearchResponse } from "@/lib/schemas/product";
import { type SearchCriteria } from "@/lib/schemas/product";

const fetchProducts = async (criteria: SearchCriteria): Promise<SearchResponse> => {
    const params = new URLSearchParams();

    Object.entries(criteria).forEach(([key, value]) => {
        if (value === undefined || value === null || value === "") return;
        if (Array.isArray(value)) {
            value.forEach((item) => { if (item) params.append(key, item); });
        } else {
            params.append(key, String(value));
        }
    });

    const response = await fetch(`/api/products/search?${params.toString()}`);

    if (!response.ok) {
        throw new Error('Błąd sieci');
    }

    const rawData = await response.json();
    const parsed = SearchResponseSchema.safeParse(rawData);

    if (!parsed.success) {
        console.error("BŁĄD ZOD:", parsed.error);
        return rawData as SearchResponse;
    }

    return parsed.data;
};

export const useProductSearch = (criteria: SearchCriteria) => {
    return useQuery({
        queryKey: ['products', criteria],
        queryFn: () => fetchProducts(criteria),
    });
};