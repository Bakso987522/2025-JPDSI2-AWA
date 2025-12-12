import {Input} from "@/components/ui/input";
import {Button} from "@/components/ui/button";
import {ComboBox} from "@/components/ui/combobox";

export default function SearchForm({
    query,
    setQuery,
    setPage,
    cropName,
    setCropName,
    pestName,
    setPestName,
    activeSubstance,
    setActiveSubstance,
    productType,
    setProductType,
}: {
    query: string;
    setQuery: (query: string) => void;
    setPage: (page: number) => void;
    cropName: string;
    setCropName: (cropName: string) => void;
    pestName: string;
    setPestName: (pestName: string) => void;
    activeSubstance: string;
    setActiveSubstance: (activeSubstance: string) => void;
    productType: string;
    setProductType: (productType: string) => void;
}){
    return (
        <div className="flex gap-2 bg-muted p-4 rounded items-center justify-between sticky top-0 z-10 shadow-sm rounded-b-lg" >
            <Input
                placeholder="Szukaj produktu (np. Agrosar)..."
                value={query}
                onChange={(e) => {
                    setQuery(e.target.value)
                    setPage(0)
                }}
                className="max-w-md"
            />
            <ComboBox data={[]} selectedItem={null}
                      setSelectedItem={(item) => {
                setCropName(item.label)
                setPage(0)
            }}
                      placeholder={"Roślina"}/>
            <ComboBox data={[]} selectedItem={null}
                      setSelectedItem={(item) => {
                setPestName(item.label)
                setPage(0)
            }}
                      placeholder={"Choroba/owad"}/>
            <ComboBox data={[]} selectedItem={null}
                      setSelectedItem={(item) => {
                setActiveSubstance(item.label)
                setPage(0)
            }}
                      placeholder={"Substancja aktywna"}/>
            <ComboBox data={[]} selectedItem={null}
                      setSelectedItem={(item) => {
                setProductType(item.label)
                setPage(0)
            }}
                      placeholder={"Typ produktu"}/>

            <Button
                onClick={() => setPage(0)}
                disabled={query === ""}
            >
                Wyszukaj
            </Button>
        </div>
    )
}