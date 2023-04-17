import { Carousel } from "./components/Carousel";
import { ExploreTopBooks } from "./components/ExploreTopBooks";
import { Heroes } from "./components/Heroes";
import { LibraryServices } from "./components/LibraryServices";

export const HomePage = () => {
    return (
        // Doing this is the React specific way of saying tht we want to return each of these
        // as a single element, but we don't want to be a div, span, etc. 
        <>
            <ExploreTopBooks/>
            <Carousel/>
            <Heroes/>
            <LibraryServices/>
        </>
    );
}