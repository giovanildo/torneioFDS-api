package com.giovanildo.torneiofds.service;

import com.giovanildo.torneiofds.model.*;
import com.giovanildo.torneiofds.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TorneioService {

    private final TorneioRepository torneioRepository;
    private final CompetidorRepository competidorRepository;
    private final PartidaRepository partidaRepository;
    private final CompetidorEmCampoRepository competidorEmCampoRepository;
    private final PremioRepository premioRepository;
    private final EAtletaRepository eAtletaRepository;
    private final ClubeRepository clubeRepository;

    public List<Torneio> listarTodos() {
        return torneioRepository.findAll();
    }

    public Torneio buscarPorId(Long id) {
        return torneioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Torneio nao encontrado"));
    }

    @Transactional
    public Torneio salvar(Torneio torneio) {
        if (torneioRepository.existsByNome(torneio.getNome())) {
            throw new IllegalArgumentException("Torneio com o nome '" + torneio.getNome() + "' ja existe");
        }
        return torneioRepository.save(torneio);
    }

    @Transactional
    public void deletar(Long id) {
        Torneio torneio = buscarPorId(id);
        premioRepository.deleteByTorneioId(id);
        competidorEmCampoRepository.deleteByTorneioId(id);
        partidaRepository.deleteByTorneioId(id);
        competidorRepository.deleteByTorneioId(id);
        torneioRepository.delete(torneio);
    }

    @Transactional
    public Competidor adicionarCompetidor(Long torneioId, Long eAtletaId, Long clubeId) {
        Torneio torneio = buscarPorId(torneioId);
        EAtleta eAtleta = eAtletaRepository.findById(eAtletaId)
                .orElseThrow(() -> new NoSuchElementException("EAtleta nao encontrado"));
        Clube clube = clubeRepository.findById(clubeId)
                .orElseThrow(() -> new NoSuchElementException("Clube nao encontrado"));

        Competidor competidor = new Competidor(torneio, eAtleta, clube);
        return competidorRepository.save(competidor);
    }

    @Transactional
    public void removerCompetidor(Long competidorId) {
        competidorRepository.deleteById(competidorId);
    }

    public List<Competidor> listarCompetidores(Long torneioId) {
        return competidorRepository.findByTorneioId(torneioId);
    }

    /**
     * Gera partidas round-robin (turno e returno).
     * Portado do TorneioBean.java do lombras-jsf.
     */
    @Transactional
    public List<Partida> gerarPartidas(Long torneioId) {
        Torneio torneio = buscarPorId(torneioId);
        List<Competidor> competidores = new ArrayList<>(competidorRepository.findByTorneioId(torneioId));

        if (competidores.size() < 2) {
            throw new IllegalArgumentException("Precisa de pelo menos 2 competidores");
        }

        // Se impar, adiciona null para bye
        boolean impar = competidores.size() % 2 == 1;
        if (impar) {
            competidores.add(0, null);
        }

        List<Partida> partidas = new ArrayList<>();
        int totalRodadas = competidores.size() - 1;

        // Turno
        for (int rodada = 0; rodada < totalRodadas; rodada++) {
            for (int jogo = 0; jogo < competidores.size() / 2; jogo++) {
                Competidor c1 = competidores.get(jogo);
                Competidor c2 = competidores.get(competidores.size() - jogo - 1);

                // Pula se bye
                if (c1 == null || c2 == null) continue;

                boolean mandoInvertido = mandoDeCampo(jogo, rodada);
                Competidor anfitriao = mandoInvertido ? c2 : c1;
                Competidor visitante = mandoInvertido ? c1 : c2;

                Partida partida = new Partida();
                partida.setTorneio(torneio);
                partida.setRodada(rodada + 1);

                CompetidorEmCampo emCasaObj = new CompetidorEmCampo(partida, anfitriao, true);
                CompetidorEmCampo foraObj = new CompetidorEmCampo(partida, visitante, false);
                partida.getCompetidoresEmCampo().add(emCasaObj);
                partida.getCompetidoresEmCampo().add(foraObj);

                partidas.add(partida);
            }

            // Gira competidores no sentido horario, mantendo o primeiro fixo
            Competidor removido = competidores.remove(competidores.size() - 1);
            competidores.add(1, removido);
        }

        // Returno — inverte mando de campo
        int partidasTurno = partidas.size();
        for (int i = 0; i < partidasTurno; i++) {
            Partida turno = partidas.get(i);

            Partida returno = new Partida();
            returno.setTorneio(torneio);
            returno.setRodada(turno.getRodada() + totalRodadas);

            CompetidorEmCampo anfTurno = turno.getAnfitriao();
            CompetidorEmCampo visTurno = turno.getVisitante();

            CompetidorEmCampo anfReturno = new CompetidorEmCampo(returno, visTurno.getCompetidor(), true);
            CompetidorEmCampo visReturno = new CompetidorEmCampo(returno, anfTurno.getCompetidor(), false);
            returno.getCompetidoresEmCampo().add(anfReturno);
            returno.getCompetidoresEmCampo().add(visReturno);

            partidas.add(returno);
        }

        partidaRepository.saveAll(partidas);
        return partidas;
    }

    private boolean mandoDeCampo(int jogo, int rodada) {
        return (jogo % 2 == 1 || (rodada % 2 == 1 && jogo == 0));
    }

    public List<Partida> listarPartidas(Long torneioId) {
        return partidaRepository.findByTorneioIdOrderByRodada(torneioId);
    }

    @Transactional
    public void registrarResultado(Long partidaId, int golsAnfitriao, int golsVisitante) {
        Partida partida = partidaRepository.findById(partidaId)
                .orElseThrow(() -> new NoSuchElementException("Partida nao encontrada"));

        partida.getAnfitriao().setGols(golsAnfitriao);
        partida.getVisitante().setGols(golsVisitante);
        partida.setEncerrada(true);

        partidaRepository.save(partida);
    }

    /**
     * Calcula a classificacao do torneio baseado nas partidas encerradas.
     */
    public List<Classificacao> calcularClassificacao(Long torneioId) {
        List<Competidor> competidores = competidorRepository.findByTorneioId(torneioId);
        List<Partida> partidas = partidaRepository.findByTorneioIdOrderByRodada(torneioId);

        Map<Long, Classificacao> mapa = new LinkedHashMap<>();
        for (Competidor c : competidores) {
            Classificacao cl = new Classificacao();
            cl.setNomeEAtleta(c.getEAtleta().getNome());
            cl.setNomeclube(c.getClube().getNome());
            mapa.put(c.getId(), cl);
        }

        for (Partida p : partidas) {
            if (!p.isEncerrada()) continue;

            CompetidorEmCampo anf = p.getAnfitriao();
            CompetidorEmCampo vis = p.getVisitante();
            if (anf == null || vis == null) continue;

            Classificacao clAnf = mapa.get(anf.getCompetidor().getId());
            Classificacao clVis = mapa.get(vis.getCompetidor().getId());
            if (clAnf == null || clVis == null) continue;

            clAnf.setJogos(clAnf.getJogos() + 1);
            clVis.setJogos(clVis.getJogos() + 1);

            clAnf.setGolsPro(clAnf.getGolsPro() + anf.getGols());
            clAnf.setGolsContra(clAnf.getGolsContra() + vis.getGols());
            clVis.setGolsPro(clVis.getGolsPro() + vis.getGols());
            clVis.setGolsContra(clVis.getGolsContra() + anf.getGols());

            if (anf.getGols() > vis.getGols()) {
                clAnf.setPontos(clAnf.getPontos() + 3);
                clAnf.setVitorias(clAnf.getVitorias() + 1);
                clVis.setDerrotas(clVis.getDerrotas() + 1);
            } else if (anf.getGols() < vis.getGols()) {
                clVis.setPontos(clVis.getPontos() + 3);
                clVis.setVitorias(clVis.getVitorias() + 1);
                clAnf.setDerrotas(clAnf.getDerrotas() + 1);
            } else {
                clAnf.setPontos(clAnf.getPontos() + 1);
                clVis.setPontos(clVis.getPontos() + 1);
                clAnf.setEmpates(clAnf.getEmpates() + 1);
                clVis.setEmpates(clVis.getEmpates() + 1);
            }
        }

        return mapa.values().stream()
                .sorted()
                .collect(Collectors.toList());
    }
}
