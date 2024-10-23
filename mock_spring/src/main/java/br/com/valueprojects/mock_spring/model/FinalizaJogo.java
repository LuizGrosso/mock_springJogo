package br.com.valueprojects.mock_spring.model;

import java.util.Calendar;
import java.util.List;

import br.com.valueprojects.mock_spring.SmsService;
import infra.JogoDao;



public class FinalizaJogo {

	private int total = 0;
	private final JogoDao dao;
	private JogoDao jogoDao;  
    private SmsService smsService; 

	public FinalizaJogo(JogoDao dao, SmsService smsService) {
		this.dao = dao;
		this.smsService = smsService;
	}

	public void finaliza() {
		List<Jogo> todosJogosEmAndamento = dao.emAndamento();

		for (Jogo jogo : todosJogosEmAndamento) {
			if (iniciouSemanaAnterior(jogo)) {
				jogo.finaliza();
				total++;
				dao.atualiza(jogo);
			}
		}
	}

	private boolean iniciouSemanaAnterior(Jogo jogo) {
		return diasEntre(jogo.getData(), Calendar.getInstance()) >= 7;
	}

	private int diasEntre(Calendar inicio, Calendar fim) {
		Calendar data = (Calendar) inicio.clone();
		int diasNoIntervalo = 0;
		while (data.before(fim)) {
			data.add(Calendar.DAY_OF_MONTH, 1);
			diasNoIntervalo++;
		}

		return diasNoIntervalo;
	}

	public int getTotalFinalizados() {
		return total;
	}
	
	  public void finalizar(Jogo jogo) {
	        jogoDao.salva(jogo);

	        String vencedor = jogo.getVencedor();
	        if (vencedor != null && !vencedor.isEmpty()) {
	            smsService.enviarSms(vencedor, "Parabéns " + vencedor + ", você venceu o jogo!");
	        }
	    }
}
