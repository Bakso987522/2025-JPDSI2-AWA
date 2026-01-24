"use client";

import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { ArrowLeft } from "lucide-react";
import { cn } from "@/lib/utils";

interface BackButtonProps {
    className?: string;
    variant?: "default" | "destructive" | "outline" | "secondary" | "ghost" | "link";
    label?: string;
}

export function BackButton({ className, variant = "outline", label = "Wróć" }: BackButtonProps) {
    const router = useRouter();

    return (
        <Button
            variant={variant}
            size="sm"
            className={cn("gap-2", className)}
            onClick={() => router.back()}
        >
            <ArrowLeft className="h-4 w-4" />
            {label}
        </Button>
    );
}