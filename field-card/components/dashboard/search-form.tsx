import {Input} from "@/components/ui/input";
import {Button} from "@/components/ui/button";

export default function SearchForm(){
    return (
     <div className="flex flex-col gap-4 bg-muted p-4 rounded-xl">
         <h2>Search Plant Protection Product</h2>
         <Input placeholder="Search..." />
         <Button>Search</Button>
     </div>
    )
}