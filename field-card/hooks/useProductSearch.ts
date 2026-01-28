import { useQuery } from "@tanstack/react-query";
import { client } from "@/lib/api-client";
import {
    SearchCriteriaSchema,
    type SearchCriteria,
    type SearchResponse,
    SearchResponseSchema
} from "@/lib/schemas/product";
import { z } from "zod";


type SearchInput = z.input<typeof SearchCriteriaSchema>;

const fetchProducts = async (input: SearchInput): Promise<SearchResponse> => {

    const criteria = SearchCriteriaSchema.parse(input);

    const params = new URLSearchParams();

    Object.entries(criteria).forEach(([key, value]) => {
        if (value === undefined || value === null || value === "") return;

        if (Array.isArray(value)) {
            if (value.length > 0) {
                value.forEach((item) => params.append(key, item));
            }
        } else {
            params.append(key, String(value));
        }
    });


    const response = await client<unknown>(`/api/products/search?${params.toString()}`);

    const parsed = SearchResponseSchema.safeParse(response);

    if (!parsed.success) {
        console.error("Błąd walidacji odpowiedzi z API:", parsed.error);
        return response as SearchResponse;
    }

    return parsed.data;
};

export const useProductSearch = (criteria: SearchInput) => {
    return useQuery({
        queryKey: ['products', criteria],

        queryFn: () => fetchProducts(criteria),
        // placeholderData: (previousData) => previousData,
    });
};