import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { client } from "@/lib/api-client";

export interface StockItem {
    id: number;
    productId: number;
    productName: string;
    manufacturer: string;
    quantity: number;
    unit: string;
    batchNumber: string;
    expirationDate: string;
}

export interface AddStockPayload {
    productId: number;
    quantity: number;
    unit: string;
    batchNumber?: string;
    expirationDate?: string;
    purchaseDate?: string;
}

export const useInventory = () => {
    const queryClient = useQueryClient();

    const query = useQuery({
        queryKey: ['inventory'],
        queryFn: () => client<StockItem[]>('/api/inventory'),
    });

    const addMutation = useMutation({
        mutationFn: (newStock: AddStockPayload) =>
            client<StockItem>('/inventory', { data: newStock }),

        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['inventory'] });
        }
    });

    const deleteMutation = useMutation({
        mutationFn: (id: number) =>
            client(`/inventory/${id}`, { method: 'DELETE' }),

        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['inventory'] });
        }
    });

    return {
        items: query.data || [],
        isLoading: query.isLoading,
        isError: query.isError,
        error: query.error,
        addItem: addMutation.mutateAsync,
        deleteItem: deleteMutation.mutateAsync,
        isAdding: addMutation.isPending,
        isDeleting: deleteMutation.isPending
    };
};