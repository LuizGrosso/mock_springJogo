package br.com.valueprojects.mock_spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.com.valueprojects.mock_spring.builder.CriadorDeJogo;
import br.com.valueprojects.mock_spring.model.FinalizaJogo;
import br.com.valueprojects.mock_spring.model.Jogo;
import infra.JogoDao;

public class FinalizaJogoTest {
    
    @Mock
    private JogoDao jogoDao; 

    @Mock
    private SmsService smsService;

    @InjectMocks
    private FinalizaJogo finalizaJogo; 

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void deveFinalizarJogosDaSemanaAnterior() {

        Calendar antiga = Calendar.getInstance();
        antiga.set(1999, 1, 20);

        Jogo jogo1 = new CriadorDeJogo().para("Caça moedas").naData(antiga).constroi();
        Jogo jogo2 = new CriadorDeJogo().para("Derruba barreiras").naData(antiga).constroi();

        List<Jogo> jogosAnteriores = Arrays.asList(jogo1, jogo2);


        when(jogoDao.emAndamento()).thenReturn(jogosAnteriores);


        finalizaJogo.finaliza();

        assertTrue(jogo1.isFinalizado());
        assertTrue(jogo2.isFinalizado());
        assertEquals(2, finalizaJogo.getTotalFinalizados());
    }

    @Test
    public void deveVerificarSeMetodoAtualizaFoiInvocado() {

        Calendar antiga = Calendar.getInstance();
        antiga.set(1999, 1, 20);

        Jogo jogo1 = new CriadorDeJogo().para("Cata moedas").naData(antiga).constroi();
        Jogo jogo2 = new CriadorDeJogo().para("Derruba barreiras").naData(antiga).constroi();

        List<Jogo> jogosAnteriores = Arrays.asList(jogo1, jogo2);

        when(jogoDao.emAndamento()).thenReturn(jogosAnteriores);

        finalizaJogo.finaliza();


        verify(jogoDao, times(1)).atualiza(jogo1);
        verify(jogoDao, times(1)).atualiza(jogo2);
    }

    @Test
    public void deveSalvarJogoEAposEnviarSms() {

        Jogo jogo = new Jogo(null);
        jogo.setVencedor("Jogador1");

        when(jogoDao.salva(jogo)).thenReturn(jogo);

        finalizaJogo.finalizar(jogo);


        verify(jogoDao).salva(jogo);
        verify(smsService).enviarSms(jogo.getVencedor(), "Parabéns Jogador1, você venceu o jogo!");
    }

    @Test
    public void naoDeveEnviarSmsSeNaoSalvarJogo() {

        Jogo jogo = new Jogo(null);
        jogo.setVencedor("Jogador2");

        when(jogoDao.salva(jogo)).thenThrow(new RuntimeException("Erro ao salvar jogo"));

        assertThrows(RuntimeException.class, () -> {
            finalizaJogo.finalizar(jogo);
        });

        verifyNoInteractions(smsService); 
    }
}
