"use client";

import { useState } from "react";
import { Check, ChevronsUpDown, Loader2, Search } from "lucide-react";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import {
    Command,
    CommandEmpty,
    CommandGroup,
    CommandInput,
    CommandItem,
    CommandList,
} from "@/components/ui/command";
import {
    Popover,
    PopoverContent,
    PopoverTrigger,
} from "@/components/ui/popover";
import { useProductSearch } from "@/hooks/useProductSearch";
import { useDebounce } from "@/hooks/useDebounce";

interface ProductAutocompleteProps {
    onSelect: (item: { id: string; name: string }) => void;
}

export function ProductAutocomplete({ onSelect }: ProductAutocompleteProps) {
    const [open, setOpen] = useState(false);
    const [value, setValue] = useState("");
    const [query, setQuery] = useState("");

    const debouncedQuery = useDebounce(query, 500);

    const { data, isLoading } = useProductSearch({
        query: debouncedQuery,
        page: 0,
        size: 10
    });

    const products = data?.results || [];

    return (
        <Popover open={open} onOpenChange={setOpen}>
            <PopoverTrigger asChild>
                <Button
                    variant="outline"
                    role="combobox"
                    aria-expanded={open}
                    className="w-full justify-between"
                >
                    {value ? value : "Wyszukaj środek..."}
                    <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                </Button>
            </PopoverTrigger>
            <PopoverContent className="w-[400px] p-0" align="start">
                <Command shouldFilter={false}>
                    <div className="flex items-center border-b px-3" cmdk-input-wrapper="">
                        <Search className="mr-2 h-4 w-4 shrink-0 opacity-50" />
                        <input
                            className="flex h-11 w-full rounded-md bg-transparent py-3 text-sm outline-none placeholder:text-muted-foreground disabled:cursor-not-allowed disabled:opacity-50"
                            placeholder="Wpisz nazwę środka..."
                            value={query}
                            onChange={(e) => setQuery(e.target.value)}
                        />
                    </div>
                    <CommandList>
                        {isLoading && (
                            <div className="p-4 text-center text-sm text-muted-foreground">
                                <Loader2 className="h-4 w-4 animate-spin inline mr-2"/>Szukam...
                            </div>
                        )}

                        {!isLoading && products.length === 0 && query.length > 2 && (
                            <CommandEmpty>Nie znaleziono środka.</CommandEmpty>
                        )}

                        <CommandGroup heading="Wyniki">
                            {products.map((product: any) => (
                                <CommandItem
                                    key={product.id}
                                    value={product.name}
                                    onSelect={() => {
                                        setValue(product.name);

                                        onSelect({
                                            id: product.id.toString(),
                                            name: product.name
                                        });

                                        setOpen(false);
                                    }}
                                >
                                    <Check
                                        className={cn(
                                            "mr-2 h-4 w-4",
                                            value === product.name ? "opacity-100" : "opacity-0"
                                        )}
                                    />
                                    <div className="flex flex-col">
                                        <span>{product.name}</span>
                                        <span className="text-[10px] text-muted-foreground">
                          {product.activeSubstance || "Substancja nieznana"}
                      </span>
                                    </div>
                                </CommandItem>
                            ))}
                        </CommandGroup>
                    </CommandList>
                </Command>
            </PopoverContent>
        </Popover>
    );
}