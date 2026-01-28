import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { client } from "@/lib/api-client";
export interface Field {
    id: number;
    name: string;
    area: number;
    description?: string;
    parcelNumbers: string[];
}

export interface CreateFieldPayload {
    name: string;
    area: number;
    description?: string;
    parcelIds: string[];
}

export const useFields = () => {
    const queryClient = useQueryClient();

    const query = useQuery({
        queryKey: ['fields'],
        queryFn: () => client<Field[]>('/api/fields'),
    });

    const addMutation = useMutation({
        mutationFn: (data: CreateFieldPayload) => {
            return client<Field>('/api/fields', { data });
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['fields'] });
        },
    });

    const deleteMutation = useMutation({
        mutationFn: (id: number) => {
            return client(`/api/fields/${id}`, { method: 'DELETE' });
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['fields'] });
        },
    });

    return {
        data: query.data || [],
        isLoading: query.isLoading,
        isError: query.isError,

        addField: addMutation.mutateAsync,
        deleteField: deleteMutation.mutateAsync,

        isAdding: addMutation.isPending,
        isDeleting: deleteMutation.isPending
    };
};