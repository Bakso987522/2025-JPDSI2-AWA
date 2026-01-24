import {useQuery} from "@tanstack/react-query";
export type DictionaryItem = {
    id: number;
    name: string;
}
export type DictionaryType = 'crops' | 'pests' | 'substances' | 'types';
const fetchDictionary = async (type: DictionaryType, query: string): Promise<DictionaryItem[]> => {
    if (!query) return [];

    const response = await fetch(`/api/dictionaries/${type}?query=${encodeURIComponent(query)}`);

    if (!response.ok) {
        throw new Error(`Błąd pobierania słownika: ${type}`);
    }

    return response.json();
};
export const useDictionary = (type: DictionaryType, query: string) => {
    return useQuery({
        queryKey: ['dictionary', type, query],
        queryFn: () => fetchDictionary(type, query),
        enabled: query.length > 0,
        placeholderData:(previousData) => previousData,
    });
}