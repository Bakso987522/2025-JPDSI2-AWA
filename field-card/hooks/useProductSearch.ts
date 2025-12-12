import { useQuery } from '@tanstack/react-query';
import { SearchCriteria, SearchResponse } from '@/types/product';


const fetchProducts = async (criteria: SearchCriteria): Promise<SearchResponse> => {
    const params = new URLSearchParams();

    Object.entries(criteria).forEach(([key, value]) => {
        if (value !== undefined && value !== "" && value !== null) {
            params.append(key, String(value));
        }
    });

    const response = await fetch(`/api/products/search?${params.toString()}`);

    if (!response.ok) {
        throw new Error('Błąd pobierania produktów');
    }

    return response.json();
};

export const useProductSearch = (criteria: SearchCriteria) => {
    return useQuery({
        queryKey: ['products', criteria],

        queryFn: () => fetchProducts(criteria),

        placeholderData: (previousData) => previousData,
    });
};