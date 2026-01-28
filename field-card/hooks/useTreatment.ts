import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { client } from "@/lib/api-client";

export interface TreatmentRecord {
    id: number;
    date: string;
    fieldName: string;
    items: {
        productName: string;
        activeSubstance: string;
        targetPest: string;
        dose: string;
        isOffLabel: boolean;
    }[];
}

export const useTreatments = () => {
    const queryClient = useQueryClient();

    const query = useQuery({
        queryKey: ["treatments"],
        queryFn: () => client<TreatmentRecord[]>("/api/treatments"),
    });

    const addTreatment = useMutation({
        mutationFn: (data: any) => client("/api/treatments", { data }),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ["treatments"] });
        },
    });
    const deleteTreatment = useMutation({
        mutationFn: (id: number) => client(`/api/treatments/${id}`, { method: "DELETE" }),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ["treatments"] });
        },
    });

    return {
        treatments: query.data || [],
        isLoading: query.isLoading,
        addTreatment: addTreatment.mutateAsync,
        deleteTreatment: deleteTreatment.mutateAsync
    };
};