"use client"

import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { useState } from "react";
import { useDictionary } from "@/hooks/useDictionary";
import { useDebounce } from "@/hooks/useDebounce";
import { SelectPills, DataItem } from "@/components/ui/currency-select";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { type SearchCriteria } from "@/lib/schemas/product";

const FormSchema = z.object({
    query: z.string(),
    cropName: z.array(z.string()),
    pestName: z.array(z.string()),
    activeSubstance: z.array(z.string()),
    productType: z.array(z.string()),
});

type FormValues = z.infer<typeof FormSchema>;

interface SearchFormProps {
    defaultValues: Partial<SearchCriteria>;
    onSearch: (filters: FormValues) => void;
}

export default function SearchForm({ defaultValues, onSearch }: SearchFormProps) {
    const form = useForm<FormValues>({
        resolver: zodResolver(FormSchema),
        defaultValues: {
            query: defaultValues.query || "",
            cropName: defaultValues.cropName || [],
            pestName: defaultValues.pestName || [],
            activeSubstance: defaultValues.activeSubstance || [],
            productType: defaultValues.productType || [],
        }
    });

    const [cropQuery, setCropQuery] = useState("");
    const debouncedCropQuery = useDebounce(cropQuery, 300);
    const { data: cropsRaw } = useDictionary('crops', debouncedCropQuery);
    const cropData: DataItem[] = cropsRaw?.map(c => ({ id: c.id.toString(), name: c.name })) || [];

    const [pestQuery, setPestQuery] = useState("");
    const debouncedPestQuery = useDebounce(pestQuery, 300);
    const { data: pestsRaw } = useDictionary('pests', debouncedPestQuery);
    const pestData: DataItem[] = pestsRaw?.map(p => ({ id: p.id.toString(), name: p.name })) || [];

    const [substanceQuery, setSubstanceQuery] = useState("");
    const debouncedSubstanceQuery = useDebounce(substanceQuery, 300);
    const { data: substancesRaw } = useDictionary('substances', debouncedSubstanceQuery);
    const substanceData: DataItem[] = substancesRaw?.map(s => ({ id: s.id.toString(), name: s.name })) || [];

    const [typeQuery, setTypeQuery] = useState("");
    const debouncedTypeQuery = useDebounce(typeQuery, 300);
    const { data: typesRaw } = useDictionary('types', debouncedTypeQuery);
    const typeData: DataItem[] = typesRaw?.map(t => ({ id: t.id.toString(), name: t.name })) || [];

    const onSubmit = (data: FormValues) => {
        onSearch(data);
    }

    return (
        <form onSubmit={form.handleSubmit(onSubmit)} className="flex flex-col gap-4 bg-muted p-4 rounded shadow-sm st">
            <div className="flex gap-2 items-center">
                <Input
                    placeholder="Szukaj produktu (np. Agrosar)..."
                    {...form.register("query")}
                    onKeyDown={(e) => {
                        if (e.key === "Enter") {
                            e.preventDefault();
                            form.handleSubmit(onSubmit)();
                        }
                    }}
                    className="flex-1"
                />
                <Button type="submit">
                    Wyszukaj
                </Button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                <Controller
                    control={form.control}
                    name="cropName"
                    render={({ field }) => (
                        <SelectPills
                            placeholder="Wybierz rośliny..."
                            data={cropData}
                            value={field.value}
                            onValueChange={field.onChange}
                            onSearch={setCropQuery}
                        />
                    )}
                />

                <Controller
                    control={form.control}
                    name="pestName"
                    render={({ field }) => (
                        <SelectPills
                            placeholder="Wybierz choroby/owady..."
                            data={pestData}
                            value={field.value}
                            onValueChange={field.onChange}
                            onSearch={setPestQuery}
                        />
                    )}
                />

                <Controller
                    control={form.control}
                    name="activeSubstance"
                    render={({ field }) => (
                        <SelectPills
                            placeholder="Wybierz substancje..."
                            data={substanceData}
                            value={field.value}
                            onValueChange={field.onChange}
                            onSearch={setSubstanceQuery}
                        />
                    )}
                />

                <Controller
                    control={form.control}
                    name="productType"
                    render={({ field }) => (
                        <SelectPills
                            placeholder="Wybierz typy..."
                            data={typeData}
                            value={field.value}
                            onValueChange={field.onChange}
                            onSearch={setTypeQuery}
                        />
                    )}
                />
            </div>
        </form>
    )
}