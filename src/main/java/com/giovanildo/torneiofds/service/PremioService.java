package com.giovanildo.torneiofds.service;

import com.giovanildo.torneiofds.model.*;
import com.giovanildo.torneiofds.repository.CompetidorRepository;
import com.giovanildo.torneiofds.repository.PremioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PremioService {

    private final PremioRepository premioRepository;
    private final CompetidorRepository competidorRepository;
    private final TorneioService torneioService;

    public List<Premio> listarPorEAtleta(Long eAtletaId) {
        return premioRepository.findByEAtletaId(eAtletaId);
    }

    public List<Premio> listarPorTorneio(Long torneioId) {
        return premioRepository.findByTorneioId(torneioId);
    }

    public long contarCocaColas(Long eAtletaId) {
        return premioRepository.countByEAtletaIdAndTipo(eAtletaId, TipoPremio.COCA_COLA);
    }

    /**
     * Gera premios automaticamente baseado na classificacao final do torneio.
     * Premios: Campeao, Vice, Artilheiro, Menos Vazada, Coca-Cola, Escapou da Coca-Cola.
     * Se o jogador atingir 12 Coca-Colas, recebe o Premio Ibis.
     */
    @Transactional
    public List<Premio> gerarPremios(Long torneioId) {
        if (premioRepository.existsByTorneioId(torneioId)) {
            throw new IllegalArgumentException("Premios ja foram gerados para este torneio");
        }

        Torneio torneio = torneioService.buscarPorId(torneioId);
        List<Classificacao> classificacao = torneioService.calcularClassificacao(torneioId);

        if (classificacao.size() < 2) {
            throw new IllegalArgumentException("Torneio precisa de pelo menos 2 competidores com partidas encerradas");
        }

        // Mapa de nome do eAtleta -> competidor (para buscar entidade)
        List<Competidor> competidores = competidorRepository.findByTorneioId(torneioId);
        Map<String, Competidor> porNome = new HashMap<>();
        for (Competidor c : competidores) {
            porNome.put(c.getEAtleta().getNome(), c);
        }

        List<Premio> premios = new ArrayList<>();

        // Campeao (1o lugar)
        premios.add(criarPremio(torneio, porNome, classificacao.get(0), TipoPremio.CAMPEAO));

        // Vice (2o lugar)
        premios.add(criarPremio(torneio, porNome, classificacao.get(1), TipoPremio.VICE_CAMPEAO));

        // Artilheiro (mais gols pro)
        Classificacao artilheiro = classificacao.stream()
                .max(Comparator.comparingInt(Classificacao::getGolsPro))
                .orElseThrow();
        premios.add(criarPremio(torneio, porNome, artilheiro, TipoPremio.ARTILHEIRO));

        // Menos Vazada (menos gols contra)
        Classificacao menosVazada = classificacao.stream()
                .min(Comparator.comparingInt(Classificacao::getGolsContra))
                .orElseThrow();
        premios.add(criarPremio(torneio, porNome, menosVazada, TipoPremio.MENOS_VAZADA));

        // Coca-Cola (ultimo lugar)
        Classificacao ultimo = classificacao.get(classificacao.size() - 1);
        premios.add(criarPremio(torneio, porNome, ultimo, TipoPremio.COCA_COLA));

        // Escapou da Coca-Cola (penultimo lugar)
        if (classificacao.size() >= 3) {
            Classificacao penultimo = classificacao.get(classificacao.size() - 2);
            premios.add(criarPremio(torneio, porNome, penultimo, TipoPremio.ESCAPOU_DA_COCA_COLA));
        }

        premioRepository.saveAll(premios);

        // Verifica se o ultimo lugar atingiu 12 Coca-Colas -> Premio Ibis
        Competidor compUltimo = porNome.get(ultimo.getNomeEAtleta());
        if (compUltimo != null) {
            long totalCocas = premioRepository.countByEAtletaIdAndTipo(
                    compUltimo.getEAtleta().getId(), TipoPremio.COCA_COLA);
            if (totalCocas >= 12) {
                // Verifica se ja nao tem Ibis
                long ibisExistente = premioRepository.countByEAtletaIdAndTipo(
                        compUltimo.getEAtleta().getId(), TipoPremio.IBIS);
                if (ibisExistente == 0) {
                    Premio ibis = criarPremio(torneio, porNome, ultimo, TipoPremio.IBIS);
                    premioRepository.save(ibis);
                    premios.add(ibis);
                }
            }
        }

        return premios;
    }

    private Premio criarPremio(Torneio torneio, Map<String, Competidor> porNome,
                               Classificacao cl, TipoPremio tipo) {
        Competidor comp = porNome.get(cl.getNomeEAtleta());
        Premio premio = new Premio(torneio, comp.getEAtleta(), comp.getClube(), tipo);
        premio.setPontos(cl.getPontos());
        premio.setGolsPro(cl.getGolsPro());
        premio.setGolsContra(cl.getGolsContra());
        premio.setVitorias(cl.getVitorias());
        premio.setDerrotas(cl.getDerrotas());
        return premio;
    }
}
