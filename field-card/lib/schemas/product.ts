import { z } from "zod";

export const ProductSchema = z.object({
    id: z.number(),
    sorId: z.string().optional(),
    name: z.string(),
    manufacturer: z.string().nullable().transform((val) => val || "Nieznany producent"),
    activeSubstance: z.array(z.string()).default(["Brak danych"]),
    type: z.array(z.string()).default(["Inny"]),
    crops: z.array(z.string()).default([]),
    pests: z.array(z.string()).default([]),
    authorizationNumber: z.string().optional(),
});

export const SearchCriteriaSchema = z.object({
    query: z.string().optional().catch("").default(""),
    cropName: z.array(z.string()).optional().default([]),
    pestName: z.array(z.string()).optional().default([]),
    activeSubstance: z.array(z.string()).optional().default([]),
    productType: z.array(z.string()).optional().default([]),
    page: z.coerce.number().default(0).catch(0),
    size: z.coerce.number().optional(),
});

export const SearchResponseSchema = z.object({
    results: z.array(ProductSchema),
    suggestions: z.array(ProductSchema).default([]),
    totalPages: z.number().default(0),
    totalElements: z.number().default(0),
    currentPage: z.number().default(0)
});


export const ProductUsageSchema = z.object({
    cropName: z.string().nullable().transform(v => v || "Nieznana uprawa"),
    pestName: z.string().nullable().transform(v => v || "Nieznany agrofag"),
    dose: z.string().nullable().transform(v => v || "Brak danych o dawce"),
});

export const ProductDetailsSchema = z.object({
    id: z.coerce.number(),
    sorId: z.string().optional(),
    name: z.string(),
    manufacturer: z.string().nullable().transform((val) => val || "Producent nieznany"),
    type: z.array(z.string()).default([]),
    permitNumber: z.string().nullable().transform(v => v || "Brak danych"),
    salesDeadline: z.string().nullable().optional(),
    useDeadline: z.string().nullable().optional(),
    labelUrl: z.string().nullable().optional(),
    activeSubstances: z.array(z.string()).default([]),
    usages: z.array(ProductUsageSchema).default([]),
    crops: z.array(z.string()).default([]),
    pests: z.array(z.string()).default([]),
});

export type ProductDetails = z.infer<typeof ProductDetailsSchema>;
export type Product = z.infer<typeof ProductSchema>;
export type SearchResponse = z.infer<typeof SearchResponseSchema>;
export type SearchCriteria = z.infer<typeof SearchCriteriaSchema>;