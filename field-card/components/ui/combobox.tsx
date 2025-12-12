"use client"

import * as React from "react"
import {useIsMobile} from "@/hooks/useIsMobile";
import { Button } from "@/components/ui/button"
import {
    Command,
    CommandEmpty,
    CommandGroup,
    CommandInput,
    CommandItem,
    CommandList,
} from "@/components/ui/command"
import {
    Drawer,
    DrawerContent,
    DrawerTrigger,
} from "@/components/ui/drawer"
import {
    Popover,
    PopoverContent,
    PopoverTrigger,
} from "@/components/ui/popover"

export type ComboItem = {
    value: string
    label: string
}


interface ResponsiveComboBoxProps {
    data: ComboItem[]
    selectedItem: ComboItem | null
    setSelectedItem: (item: ComboItem | null) => void
    placeholder?: string
    className?: string
}

export function ComboBox({
                                       data,
                                       selectedItem,
                                       setSelectedItem,
                                       placeholder = "Select option...",
                                       className = ""
                                   }: ResponsiveComboBoxProps) {
    const [open, setOpen] = React.useState(false)
    const isDesktop = !useIsMobile()

    if (isDesktop) {
        return (
            <Popover open={open} onOpenChange={setOpen}>
                <PopoverTrigger asChild>
                    <Button variant="outline" className="w-[150px] justify-start">
                        {selectedItem ? <>{selectedItem.label}</> : <>{placeholder}</>}
                    </Button>
                </PopoverTrigger>
                <PopoverContent className="w-[200px] p-0" align="start">
                    <SelectionList
                        data={data}
                        setOpen={setOpen}
                        setSelectedItem={setSelectedItem}
                    />
                </PopoverContent>
            </Popover>
        )
    }

    return (
        <Drawer open={open} onOpenChange={setOpen}>
            <DrawerTrigger asChild>
                <Button variant="outline" className="w-[150px] justify-start">
                    {selectedItem ? <>{selectedItem.label}</> : <>{placeholder}</>}
                </Button>
            </DrawerTrigger>
            <DrawerContent>
                <div className="mt-4 border-t">
                    <SelectionList
                        data={data}
                        setOpen={setOpen}
                        setSelectedItem={setSelectedItem}
                    />
                </div>
            </DrawerContent>
        </Drawer>
    )
}

function SelectionList({
                           data,
                           setOpen,
                           setSelectedItem,
                       }: {
    data: ComboItem[]
    setOpen: (open: boolean) => void
    setSelectedItem: (item: ComboItem | null) => void
}) {
    return (
        <Command>
            <CommandInput placeholder="Filter..." />
            <CommandList>
                <CommandEmpty>No results found.</CommandEmpty>
                <CommandGroup>
                    {data.map((item) => (
                        <CommandItem
                            key={item.value}
                            value={item.value}
                            onSelect={(value) => {
                                setSelectedItem(
                                    data.find((i) => i.value === value) || null
                                )
                                setOpen(false)
                            }}
                        >
                            {item.label}
                        </CommandItem>
                    ))}
                </CommandGroup>
            </CommandList>
        </Command>
    )
}