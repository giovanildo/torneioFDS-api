package com.giovanildo.torneiofds.controller;

import com.giovanildo.torneiofds.dto.PremioResponse;
import com.giovanildo.torneiofds.dto.SalaDeTrofeusResponse;
import com.giovanildo.torneiofds.model.Premio;
import com.giovanildo.torneiofds.model.TipoPremio;
import com.giovanildo.torneiofds.service.EAtletaService;
import com.giovanildo.torneiofds.service.PremioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Premios", description = "Sala de trofeus e premiacoes")
public class PremioController {

    private final PremioService premioService;
    private final EAtletaService eAtletaService;

    @GetMapping("/torneios/{torneioId}/premios")
    @Operation(summary = "Listar premios de um torneio")
    public List<PremioResponse> listarPremiosTorneio(@PathVariable Long torneioId) {
        return premioService.listarPorTorneio(torneioId).stream()
                .map(PremioResponse::from)
                .toList();
    }

    @GetMapping("/eatletas/{eAtletaId}/trofeus")
    @Operation(summary = "Sala de trofeus de um jogador")
    public SalaDeTrofeusResponse salaDeTrofeus(@PathVariable Long eAtletaId) {
        String nome = eAtletaService.buscarPorId(eAtletaId).getNome();
        List<Premio> premios = premioService.listarPorEAtleta(eAtletaId);

        List<PremioResponse> titulos = filtrar(premios, TipoPremio.CAMPEAO);
        List<PremioResponse> vices = filtrar(premios, TipoPremio.VICE_CAMPEAO);
        List<PremioResponse> artilheiros = filtrar(premios, TipoPremio.ARTILHEIRO);
        List<PremioResponse> menosVazadas = filtrar(premios, TipoPremio.MENOS_VAZADA);
        List<PremioResponse> cocaColas = filtrar(premios, TipoPremio.COCA_COLA);
        List<PremioResponse> escapouDaCocaCola = filtrar(premios, TipoPremio.ESCAPOU_DA_COCA_COLA);
        List<PremioResponse> ibis = filtrar(premios, TipoPremio.IBIS);

        // Total de torneios distintos em que participou (baseado nos premios)
        long totalTorneios = premios.stream()
                .map(p -> p.getTorneio().getId())
                .distinct()
                .count();

        return new SalaDeTrofeusResponse(
                nome,
                titulos, vices, artilheiros, menosVazadas, cocaColas, escapouDaCocaCola, ibis,
                (int) totalTorneios,
                titulos.size(),
                cocaColas.size(),
                !ibis.isEmpty()
        );
    }

    private List<PremioResponse> filtrar(List<Premio> premios, TipoPremio tipo) {
        return premios.stream()
                .filter(p -> p.getTipo() == tipo)
                .map(PremioResponse::from)
                .toList();
    }
}
