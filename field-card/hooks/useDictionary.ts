import { useQuery } from "@tanstack/react-query";
import { client } from "@/lib/api-client";

export type DictionaryItem = {
    id: number;
    name: string;
}

export type DictionaryType = 'crops' | 'pests' | 'substances' | 'types';

const fetchDictionary = async (type: DictionaryType, query: string): Promise<DictionaryItem[]> => {
    if (!query) return [];

    return client<DictionaryItem[]>(`/api/dictionaries/${type}?query=${encodeURIComponent(query)}`);
};

export const useDictionary = (type: DictionaryType, query: string) => {
    return useQuery({
        queryKey: ['dictionary', type, query],
        queryFn: () => fetchDictionary(type, query),
        enabled: query.length > 0,
        placeholderData: (previousData) => previousData,
    });
}