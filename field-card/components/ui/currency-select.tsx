"use client";

import React, { useState, useRef, useEffect } from "react";
import { cn } from "@/lib/utils";
import { Badge } from "@/components/ui/badge";
import {
    Popover,
    PopoverContent,
    PopoverAnchor,
} from "@/components/ui/popover";
import { X } from "lucide-react";

export interface DataItem {
    id?: string;
    value?: string;
    name: string;
}

interface SelectPillsProps {
    data: DataItem[];
    defaultValue?: string[];
    value?: string[];
    onValueChange?: (selectedValues: string[]) => void;
    placeholder?: string;
    onSearch?: (query: string) => void;
}

export const SelectPills: React.FC<SelectPillsProps> = ({
                                                            data,
                                                            defaultValue = [],
                                                            value,
                                                            onValueChange,
                                                            placeholder = "Type to search...",
                                                            onSearch,
                                                        }) => {
    const [inputValue, setInputValue] = useState<string>("");
    const [selectedPills, setSelectedPills] = useState<string[]>(
        value || defaultValue
    );
    const [isOpen, setIsOpen] = useState<boolean>(false);
    const [highlightedIndex, setHighlightedIndex] = useState<number>(-1);
    const inputRef = useRef<HTMLInputElement | null>(null);
    const radioGroupRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        if (value) {
            setSelectedPills(value);
        }
    }, [value]);

    const activePills = Array.isArray(value || selectedPills) ? (value || selectedPills) : [];

    const filteredItems = data.filter(
        (item) => !activePills.includes(item.name)
    );

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const newValue = e.target.value;
        setInputValue(newValue);
        setHighlightedIndex(-1);

        if (onSearch) {
            onSearch(newValue);
        }

        if (newValue.length > 0) {
            setIsOpen(true);
        } else {
            setIsOpen(false);
        }

        requestAnimationFrame(() => {
            inputRef.current?.focus();
        });
    };

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        switch (e.key) {
            case "ArrowDown":
                e.preventDefault();
                if (isOpen && filteredItems.length > 0) {
                    const firstRadio = radioGroupRef.current?.querySelector(
                        'input[type="radio"]'
                    ) as HTMLElement;
                    firstRadio?.focus();
                    setHighlightedIndex(0);
                }
                break;
            case "Escape":
                setIsOpen(false);
                break;
            case "Backspace":
                if (inputValue === "" && selectedPills.length > 0) {
                    handlePillRemove(selectedPills[selectedPills.length - 1]);
                }
                break;
        }
    };

    const handleRadioKeyDown = (
        e: React.KeyboardEvent<HTMLDivElement>,
        index: number
    ) => {
        switch (e.key) {
            case "ArrowDown":
                e.preventDefault();
                if (index < filteredItems.length - 1) {
                    setHighlightedIndex(index + 1);
                    const nextItem = radioGroupRef.current?.querySelector(
                        `div:nth-child(${index + 2})`
                    ) as HTMLElement;
                    if (nextItem) {
                        nextItem.scrollIntoView({
                            behavior: "smooth",
                            block: "nearest",
                        });
                    }
                }
                break;
            case "ArrowUp":
                e.preventDefault();
                if (index > 0) {
                    setHighlightedIndex(index - 1);
                    const prevItem = radioGroupRef.current?.querySelector(
                        `div:nth-child(${index})`
                    ) as HTMLElement;
                    if (prevItem) {
                        prevItem.scrollIntoView({
                            behavior: "smooth",
                            block: "nearest",
                        });
                    }
                } else {
                    inputRef.current?.focus();
                    setHighlightedIndex(-1);
                }
                break;
            case "Enter":
                e.preventDefault();
                handleItemSelect(filteredItems[index]);
                inputRef.current?.focus();
                break;
            case "Escape":
                e.preventDefault();
                setIsOpen(false);
                inputRef.current?.focus();
                break;
        }
    };

    const handleItemSelect = (item: DataItem) => {
        const newSelectedPills = [...selectedPills, item.name];
        setSelectedPills(newSelectedPills);
        setInputValue("");
        if (onSearch) onSearch("");

        setIsOpen(false);
        setHighlightedIndex(-1);
        if (onValueChange) {
            onValueChange(newSelectedPills);
        }
    };

    const handlePillRemove = (pillToRemove: string) => {
        const newSelectedPills = selectedPills.filter(
            (pill) => pill !== pillToRemove
        );
        setSelectedPills(newSelectedPills);
        if (onValueChange) {
            onValueChange(newSelectedPills);
        }
    };

    const handleOpenChange = (open: boolean) => {
        if (!open) {
            setIsOpen(false);
        }
    };

    return (
        <Popover open={isOpen} onOpenChange={handleOpenChange}>
            <div className="flex flex-wrap gap-2 min-h-10 p-2 border rounded-md bg-background ring-offset-background focus-within:ring-2 focus-within:ring-ring focus-within:ring-offset-2">
                {activePills.map((pill) => (
                    <Badge
                        key={pill}
                        variant="secondary"
                        onClick={() => handlePillRemove(pill)}
                        className="hover:cursor-pointer gap-1 group"
                    >
                        {pill}
                        <button
                            onClick={(e) => {
                                e.stopPropagation();
                                handlePillRemove(pill)
                            }}
                            className="appearance-none text-muted-foreground group-hover:text-foreground transition-colors"
                        >
                            <X size={12} />
                        </button>
                    </Badge>
                ))}
                <PopoverAnchor asChild>
                    <input
                        ref={inputRef}
                        type="text"
                        className="flex-1 bg-transparent outline-none placeholder:text-muted-foreground min-w-[80px] text-sm"
                        value={inputValue}
                        onChange={handleInputChange}
                        onKeyDown={handleKeyDown}
                        placeholder={activePills.length > 0 ? "" : placeholder}
                    />
                </PopoverAnchor>
            </div>

            {filteredItems.length > 0 && (
                <PopoverContent
                    onOpenAutoFocus={(e) => e.preventDefault()}
                    onFocusOutside={(e) => e.preventDefault()}
                    onInteractOutside={(e) => {
                        if (e.target === inputRef.current) e.preventDefault();
                    }}
                    className="p-1 w-[var(--radix-popover-trigger-width)] min-w-[300px]"
                    align="start"
                >
                    <div
                        ref={radioGroupRef}
                        role="radiogroup"
                        aria-label="Pill options"
                        onKeyDown={(e) => handleRadioKeyDown(e, highlightedIndex)}
                        className="max-h-[200px] overflow-y-auto"
                    >
                        {filteredItems.map((item, index) => (
                            <div
                                key={item.id || item.value || item.name}
                                onClick={() => handleItemSelect(item)}
                                className={cn(
                                    "relative flex cursor-pointer select-none items-center gap-2 rounded-sm px-2 py-1.5 text-sm outline-none transition-colors hover:bg-accent/70 hover:text-accent-foreground data-[disabled]:pointer-events-none data-[disabled]:opacity-50",
                                    highlightedIndex === index && "bg-accent text-accent-foreground"
                                )}
                            >
                                <span className="flex-1">{item.name}</span>
                            </div>
                        ))}
                    </div>
                </PopoverContent>
            )}
        </Popover>
    );
};