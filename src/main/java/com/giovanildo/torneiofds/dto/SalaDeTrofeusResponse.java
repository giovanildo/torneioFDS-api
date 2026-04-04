package com.giovanildo.torneiofds.dto;

import java.util.List;

public record SalaDeTrofeusResponse(
        String nomeEAtleta,
        List<PremioResponse> titulos,
        List<PremioResponse> vices,
        List<PremioResponse> terceiros,
        List<PremioResponse> artilheiros,
        List<PremioResponse> menosVazadas,
        List<PremioResponse> cocaColas,
        List<PremioResponse> ibis,
        int totalTorneios,
        int totalTitulos,
        int totalCocaColas,
        boolean temIbis
) {}
