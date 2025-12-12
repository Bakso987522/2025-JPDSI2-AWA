export interface SearchCriteria {
    query?: string;
    cropName?: string;
    pestName?: string;
    activeSubstance?: string;
    productType?: string;
    page?: number;
    size?: number;
}

export interface ProductResult {
    sorId: string;
    name: string;
    activeSubstance: string;
    manufacturer: string;
    authorizationNumber: string;
}


export interface SearchResponse {
    results: ProductResult[];
    suggestions: ProductResult[];
    totalPages: number;
    totalElements: number;
    currentPage: number;
}