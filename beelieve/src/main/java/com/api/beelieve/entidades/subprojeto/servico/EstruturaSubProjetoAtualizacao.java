package com.api.beelieve.entidades.subprojeto.servico;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.beelieve.entidade.cronograma.Progresso;
import com.api.beelieve.entidades.nivelsubprojeto.servico.EstruturaNivelSubProjetoAtualizacao;
import com.api.beelieve.entidades.projeto.Projeto;
import com.api.beelieve.entidades.subprojeto.SubProjeto;
import com.api.beelieve.entidades.subprojeto.dto.DadosEstruturaSubProjetoAtualizacao;
import com.api.beelieve.repositorio.SubProjetoRepositorio;
import com.api.beelieve.repositorio.TarefaRepositorio;

@Service
public class EstruturaSubProjetoAtualizacao{

	
	@Autowired
	private SubProjetoRepositorio repositorio_subprojeto;
	
	@Autowired
	private TarefaRepositorio repositorio_tarefa;
	
	@Autowired
	private EstruturaNivelSubProjetoAtualizacao estruturaNivelAtualizacao; 
	
	@Autowired
	private CadastroSubProjeto cadastroSubProjeto;
	
	
	@Autowired
	private DeleteSubProjeto deleteSubProjeto;
	

	private List<Progresso> insertNiveisCronograma = new ArrayList<Progresso>();
	

	
	public List<Progresso> atualizarEstrutura(List<DadosEstruturaSubProjetoAtualizacao> listaSubProjeto, Projeto projeto){
		List<SubProjeto> listaSubProjetoAtual = repositorio_subprojeto.findByProjeto(projeto);
		
		//Atualizando elementos que existem no banco
		Iterator<DadosEstruturaSubProjetoAtualizacao> iteratorDadosSubProjetoAtualizacao = listaSubProjeto.iterator();
		while(iteratorDadosSubProjetoAtualizacao.hasNext()) {
			DadosEstruturaSubProjetoAtualizacao dadosSub = iteratorDadosSubProjetoAtualizacao.next();
			Iterator<SubProjeto> iteratorSubProjetoAtual = listaSubProjetoAtual.iterator();
			while(iteratorSubProjetoAtual.hasNext()) {
				SubProjeto subProjeto = iteratorSubProjetoAtual.next();
				if(dadosSub.id_sub_projeto() == subProjeto.getSub_projeto_id()) {
					subProjeto.setNomeSubProjeto(dadosSub.nome_sub_projeto());
					subProjeto.setOrdem_sub_projeto(dadosSub.ordem_sub_projeto());
					if(dadosSub.nivel_sub_projeto() != null) {
						repositorio_tarefa.deleteAll(subProjeto.getTarefas());
						List<Progresso> listaNivel = estruturaNivelAtualizacao.atualizarEstrutura(dadosSub.nivel_sub_projeto(), subProjeto);
						if(!listaNivel.isEmpty()) {
							insertNiveisCronograma.addAll(listaNivel);
						}
					}
					
					iteratorDadosSubProjetoAtualizacao.remove();
					iteratorSubProjetoAtual.remove();
				}
			}
		}
		System.out.println(listaSubProjetoAtual);
		System.out.println(!listaSubProjetoAtual.isEmpty());
		//Criando elementos não encontrados no banco mas que estão no JSON
		if(!listaSubProjeto.isEmpty()) {
			List<SubProjeto> subProjetosNovos = new ArrayList<SubProjeto>();
			listaSubProjeto.forEach((subProj)->{
				SubProjeto novoSubProj = new SubProjeto(subProj, projeto);
				subProjetosNovos.add(novoSubProj);
				insertNiveisCronograma.add(new Progresso(novoSubProj));
				novoSubProj.getNivelSubProjeto().forEach((novoNivel)->{
					insertNiveisCronograma.add(new Progresso(novoNivel));
				});
			});
			cadastroSubProjeto.cadastroCascata(subProjetosNovos);
		};
		
		//Deletando elementos que não estão no JSON
		if(!listaSubProjetoAtual.isEmpty()) {
			deleteSubProjeto.deleteCascata(listaSubProjetoAtual);
		}
		
		return insertNiveisCronograma;
	}
}