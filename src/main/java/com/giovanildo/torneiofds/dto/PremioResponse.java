package com.giovanildo.torneiofds.dto;

import com.giovanildo.torneiofds.model.Premio;

public record PremioResponse(
        Long id,
        String tipo,
        String nomeTorneio,
        String dataTorneio,
        String nomeClube,
        int pontos,
        int golsPro,
        int golsContra,
        int vitorias,
        int derrotas
) {
    public static PremioResponse from(Premio p) {
        return new PremioResponse(
                p.getId(),
                p.getTipo().name(),
                p.getTorneio().getNome(),
                p.getTorneio().getDataTorneio().toString(),
                p.getClube().getNome(),
                p.getPontos(),
                p.getGolsPro(),
                p.getGolsContra(),
                p.getVitorias(),
                p.getDerrotas()
        );
    }
}
