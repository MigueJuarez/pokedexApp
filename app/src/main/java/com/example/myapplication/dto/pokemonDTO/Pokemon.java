package com.example.myapplication.dto.pokemonDTO;

public class Pokemon {

    private final String name;
    private final String img;
    private final String number;
    private final String baseExperience;
    private final String pokemonSpeciesUrl;

    public Pokemon(String name, int number, String baseExperience) {
        this.name = name;
        this.number = String.valueOf(number);
        this.img = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/home/" + number + ".png";
        this.baseExperience = baseExperience;
        this.pokemonSpeciesUrl = "https://pokeapi.co/api/v2/pokemon-species/" + number;
    }

    public String getName() {
        return name;
    }
    
    public String getImg() {
        return img;
    }

    public String getNumber() {
        return number;
    }

    public String getPokemonSpeciesUrl() { return pokemonSpeciesUrl;}
}
