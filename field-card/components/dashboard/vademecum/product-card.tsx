import Link from "next/link";
import {
    Card,
    CardContent,
    CardFooter,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import {
    Leaf,
    Bug,
    Droplet,
    FlaskConical,
    Factory,
    ChevronRight,
    ShieldCheck
} from "lucide-react";
import { cn } from "@/lib/utils";
import {
    Tooltip,
    TooltipContent,
    TooltipProvider,
    TooltipTrigger,
} from "@/components/ui/tooltip"


interface ProductCardProps {
    product: {
        id: string;
        name: string;
        manufacturer: string;
        type: string[];
        activeSubstances: string[];
        crops: string[];
        pests: string[];
        dosage?: string;
    };
}

export function ProductCard({ product }: ProductCardProps) {
    const substances = product.activeSubstances || [];
    const visibleSubstances = substances.slice(0, 3);
    const hiddenSubstances = substances.slice(3);

    return (
        <Card className="group flex flex-col overflow-hidden border transition-all hover:shadow-md hover:border-primary/50 h-full">
            <div className="px-6 py-1 flex flex-col gap-1">
                <div className="flex justify-between items-start">
                    <h3 className="font-bold text-lg leading-tight tracking-wide line-clamp-1" title={product.name}>
                        {product.name}
                    </h3>
                    <ShieldCheck className="w-5 h-5 opacity-80" />
                </div>
                <div className="flex items-center gap-1.5 text-xs font-medium opacity-90">
                    <Factory className="w-3.5 h-3.5" />
                    <span className="truncate">{product.manufacturer}</span>
                </div>
            </div>

            <CardContent className="flex-1 p-5 space-y-5">

                <div className="space-y-3">
                    <div className="flex flex-wrap gap-2 ml-4 mb-4">
                        {product.type.map((t) => (
                            <Badge key={t} variant="outline" className="font-semibold capitalize">
                                {t}
                            </Badge>
                        ))}
                    </div>

                    <div className="space-y-1.5 p-2 ml-2">
                        <p className="text-xs font-semibold text-muted-foreground uppercase flex items-center gap-1.5">
                            <FlaskConical className="w-3.5 h-3.5" /> Substancje czynne
                        </p>
                        <div className="flex flex-wrap gap-1.5 p-2">
                            {visibleSubstances.length > 0 ? (
                                visibleSubstances.map((sub, i) => (
                                    <span key={i} className="inline-flex items-center px-2 py-1 rounded bg-muted text-xs font-medium text-foreground border">
                        {sub}
                    </span>
                                ))
                            ) : (
                                <span className="text-xs text-muted-foreground italic">Brak danych</span>
                            )}

                            {hiddenSubstances.length > 0 && (
                                <TooltipProvider>
                                    <Tooltip>
                                        <TooltipTrigger asChild>
                            <span className="text-xs text-muted-foreground py-1 px-1 cursor-help hover:text-primary transition-colors underline decoration-dotted underline-offset-4">
                                +{hiddenSubstances.length} więcej
                            </span>
                                        </TooltipTrigger>
                                        <TooltipContent>
                                            <div className="flex flex-col gap-1">
                                                <p className="font-semibold text-xs mb-1">Pozostałe substancje:</p>
                                                {hiddenSubstances.map((sub, i) => (
                                                    <span key={i} className="text-xs">• {sub}</span>
                                                ))}
                                            </div>
                                        </TooltipContent>
                                    </Tooltip>
                                </TooltipProvider>
                            )}
                        </div>
                    </div>
                </div>

                <Separator />

                <div className="grid grid-cols-1 gap-3 text-sm">

                    <div className="flex items-start gap-2.5 p-2">
                        <div className="p-1.5 rounded-full bg-emerald-100 text-emerald-700 shrink-0">
                            <Leaf className="w-3.5 h-3.5" />
                        </div>
                        <div className="flex-1 min-w-0">
                            <span className="text-xs font-semibold text-muted-foreground block">Uprawy</span>
                            <p className="leading-snug text-foreground/90 truncate" title={product.crops.join(", ")}>
                                {product.crops.length > 0 ? product.crops.join(", ") : "Brak danych"}
                            </p>
                        </div>
                    </div>

                    <div className="flex items-start gap-2.5 p-2">
                        <div className="mt-0.5 p-1.5 rounded-full bg-rose-100 text-rose-700 shrink-0">
                            <Bug className="w-3.5 h-3.5" />
                        </div>
                        <div className="flex-1 min-w-0">
                            <span className="text-xs font-semibold text-muted-foreground block">Zwalcza</span>
                            <p className="leading-snug text-foreground/90 truncate" title={product.pests.join(", ")}>
                                {product.pests.length > 0 ? product.pests.join(", ") : "Brak danych"}
                            </p>
                        </div>
                    </div>

                    {product.dosage && (
                        <div className="flex items-center gap-2.5 pt-1">
                            <Droplet className="w-4 h-4 text-sky-500 ml-1" />
                            <span className="font-mono text-sm font-medium text-foreground">
                        {product.dosage}
                    </span>
                        </div>
                    )}
                </div>

            </CardContent>

            <CardFooter className="p-4 pt-0 mt-auto border-t bg-muted/20">
                <Button asChild variant="ghost" className="w-full justify-between hover:bg-transparent hover:text-primary group-hover:translate-x-1 transition-transform pl-0">
                    <Link href={`/dashboard/vademecum/${product.id}`}>
                        <span className="font-semibold">Zobacz szczegóły</span>
                        <ChevronRight className="w-4 h-4 ml-2" />
                    </Link>
                </Button>
            </CardFooter>
        </Card>
    );
}