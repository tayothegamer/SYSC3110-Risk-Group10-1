package Model;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Hasan Issa
 * <p>
 * This is the Map object of our Risk world, which we represent as a Graph
 * Every Country is a Vertex, which has neighbouring countries connected to its Edges.
 */

@XmlRootElement
public class Map {

    @XmlTransient
    private Graph<Country, DefaultEdge> mapGraph;

    private Set<Edge> listOfEdges;

    private Set<Country> listOfCountries;
    private String mapBackgroundFileName;

    public ArrayList<Continent> getContinents() {
        return continents;
    }
    @XmlElementWrapper(name="Continents")
    @XmlElement(name="Continent")
    public void setContinents(ArrayList<Continent> continents) {
        this.continents = continents;
    }

    private ArrayList<Continent> continents;

    public Map() {
    }

    public Set<Edge> getListOfEdges() {
        return listOfEdges;
    }

    @XmlElementWrapper(name="Neighbours")
    @XmlElement(name="Neighbour")
    public void setListOfEdges(Set<Edge> listOfEdges) {
        this.listOfEdges = listOfEdges;
    }

    public Graph<Country, DefaultEdge> getMapGraph() {
        return mapGraph;
    }
    @XmlTransient
    public void setMapGraph(Graph<Country, DefaultEdge> mapGraph) {
        this.mapGraph = mapGraph;
    }

    public Set<Country> getListOfCountries() {
        return listOfCountries;
    }
    @XmlElementWrapper(name = "Countries")
    @XmlElement(name="Country")
    public void setListOfCountries(Set<Country> listOfCountries) {
        this.listOfCountries = listOfCountries;
    }

    public Set<Country> getAllCountries() {
        return listOfCountries;
    }

    public Country getCountryByName(String CountryName) {
        for (Country c : listOfCountries) {
            if (c.getName().toLowerCase().equals(CountryName.toLowerCase())) {
                return c;
            }
        }
        return null;
    }

    public List<Country> getCountryNeighbours(Country country) {
        List<Country> neighbours = new ArrayList<Country>();
        if (listOfCountries.contains(country)) {
            for (DefaultEdge e : mapGraph.edgesOf(country)) {
                if (!mapGraph.getEdgeSource(e).equals(country)) {
                    neighbours.add(mapGraph.getEdgeSource(e));
                }
                if (!mapGraph.getEdgeTarget(e).equals(country)) {
                    neighbours.add(mapGraph.getEdgeTarget(e));
                }
            }
        }
        neighbours.remove(country);
        return neighbours;
    }

    public boolean areNeighbours(String firtCountryName, String secondCountryName) {
        if (getCountryNeighbours(getCountryByName(firtCountryName)).contains(getCountryByName(secondCountryName)))
            return true;
        else return false;
    }

    public boolean ownedBySamePlayer(String firstCountry, String secondCountry) {
        if (getCountryByName(firstCountry).getPlayer() == getCountryByName(secondCountry).getPlayer()) return true;
        else return false;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        listOfCountries.forEach(x -> sb.append(x + "\n"));
        return sb.toString();
    }

    public boolean pathBetweenSourceAndDestination(Country theSourceCountry, Country theDestinationCountry, int currentLengthAway, int currentPlayer, List<Country> visitedCountries) {
        //Start at the source country
        //Get the source neighbours owned by the same player that owns the source country
        //Is the destination in one of those neighbours?
        //replace source with the first neighbour owned by the same player, repeat the algorithm until the destination is in the neighbours
        if (currentLengthAway >= 15) {
            return false;
        } else if (theSourceCountry.equals(theDestinationCountry)) {
            return true;
        } else if (theSourceCountry.getPlayer().getPlayerNumber() == currentPlayer) {

            if(visitedCountries==null) {
                visitedCountries = new ArrayList<>();
                visitedCountries.add(theSourceCountry);
            } else if(visitedCountries.contains(theSourceCountry)) return false;
            else {
                visitedCountries.add(theSourceCountry);
            }

            currentLengthAway++;
            for (Country countryNeighbour : getCountryNeighbours(theSourceCountry)) {
                boolean result = false;
                result = pathBetweenSourceAndDestination(countryNeighbour, theDestinationCountry, currentLengthAway, currentPlayer, visitedCountries);
                if(result) return true;
            }
            return  false;
        } else {
            return false;
        }
    }
    public String toXML(){
        String xml = "error converting to XML";
        try {
            JAXBContext context = JAXBContext.newInstance(Map.class);
            Marshaller marshaller= context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(this, stringWriter);
            xml = stringWriter.toString();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return xml;
    }
    public void importFromXmlFile(String filename){
        try {
            JAXBContext context = JAXBContext.newInstance(Map.class);
            Map xmlObjRead =  (Map) context.createUnmarshaller().unmarshal(this.getClass().getClassLoader().getResourceAsStream(filename));
            this.listOfCountries= xmlObjRead.getListOfCountries();
            this.listOfEdges=xmlObjRead.getListOfEdges();
            this.mapBackgroundFileName = xmlObjRead.mapBackgroundFileName;
            this.continents=xmlObjRead.getContinents();
            this.initMap();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private void initMap() {
        this.mapGraph = new SimpleGraph<>(DefaultEdge.class);
        this.listOfCountries.forEach(x -> this.mapGraph.addVertex(new Country(x.getName())));
        this.listOfEdges.forEach(x -> {
            Country c1 = null, c2 = null;
            for (Country c : listOfCountries) {
                if(c.equals(x.firstCountry)) {
                    c1 = c;
                } else if (c.equals(x.secondCountry)) {
                    c2 = c;
                }
            }
            this.mapGraph.addEdge(c1, c2);
        });
    }

    public static void main(String[] args) {
        Map m= new Map();
        m.importFromXmlFile("defaultMap.xml");
        System.out.println(m);
        System.out.println(m.getContinents().toString());
    }

    public String getMapBackgroundFileName() {
        return mapBackgroundFileName;
    }

    @XmlElement(name = "mapBackgroundFileName")
    public void setMapBackgroundFileName(String mapBackgroundFileName) {
        this.mapBackgroundFileName = mapBackgroundFileName;
    }
}
