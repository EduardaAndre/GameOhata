package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class FlappyBird extends ApplicationAdapter {

    private SpriteBatch batch;//Classe responsável pelo desenho da Sprite(Sprite Batch).
    private Texture[] passaros;
    private Texture fundo;
    private Texture canoBaixo;
    private Texture canoTopo;
    private Texture gameOver;
    private Texture moedaOuro;
    private Texture moedaPrata;
    private Texture moedaAtual;
    private Texture titulo;


    private ShapeRenderer shapeRenderer;//Classe responsável pela colisão.
    private Circle circuloPassaro;
    private Circle circuloMoeda;
    private Rectangle retanguloCanoCima;
    private Rectangle retanguloCanoBaixo;

    private float larguraDispositivo;//classes na qual define o espaçamento da tela.
    private float alturaDispositivo;
    private float variacao = 0;
    private float gravidade = 2;
    private float posicaoInicialVerticalPassaro = 0;
    private float posicaoCanoHorizontal;
    private float posicaoCanoVertical;
    private float espacoEntreCanos;
    private Random random;
    private int pontos = 0;
    private int pontuacaoMaxima = 0;
    private boolean passouCano = false;
    private int estadoJogo = 0;
    private float posicaoHorizontalPassaro = 0;
    private float scalePassaro = .3f;
    private float posicaoMoedaVertical;
    private float posicaoMoedaHorizontal;
    private float scaleMoeda = .2f;

    BitmapFont textoPontuacao;//mapeamento da fonte
    BitmapFont textoReiniciar;
    BitmapFont textoMelhorPontuacao;

    Sound somVoando;//Som do jogo 
    Sound somColisao;
    Sound somPontuacao;
    Sound moedaSom;

    Preferences preferencias;//preferencias

    private OrthographicCamera camera;//Classe camera do jogo
    private Viewport viewport;
    private final float VIRTUAL_WIDTH = 720;
    private final float VIRTUAL_HEIGHT = 1280;

    @Override
    public void create()//Aqui é onde inicia o programa que faz com que inicia as Texturas e Objetos.
    {
        inicializarTexturas();
        inicializaObjetos();
    }


    @Override
    public void render()//Update(chama cada frame).
    {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        verificarEstadoJogo();
        validarPontos();
        desenharTexturas();
        detectarColisoes();
    }


    private void inicializarTexturas()//Onde é definida as texturas aparente no jogo. 
    {
        passaros = new Texture[3];
        passaros[0] = new Texture("passaro1.png");
        passaros[1] = new Texture("passaro2.png");
        passaros[2] = new Texture("passaro3.png");

        fundo = new Texture("fundo.png");
        canoBaixo = new Texture("cano_baixo_maior.png");
        canoTopo = new Texture("cano_topo_maior.png");
        gameOver = new Texture("game_over.png");

        moedaOuro = new Texture("ouro_moeda.png");
        moedaPrata = new Texture("prata_moeda.png");
        moedaAtual = moedaOuro;

        titulo = new Texture("icone_lambu.png");
    }

    private void inicializaObjetos()//Onde é criado a inicizalização dos Objetos.
    {
        batch = new SpriteBatch();//desenho da Sprite
        random = new Random();//seria tipo uma classe na qual é definido um valor aleatorio.

        larguraDispositivo = VIRTUAL_WIDTH;
        alturaDispositivo = VIRTUAL_HEIGHT;
        posicaoInicialVerticalPassaro = alturaDispositivo / 2;
        posicaoCanoHorizontal = larguraDispositivo;
        posicaoMoedaHorizontal = posicaoCanoHorizontal + larguraDispositivo / 2;
        posicaoMoedaVertical = alturaDispositivo / 2;
        espacoEntreCanos = 350;//Aqui é onde é calculado o passaro com o espaçamento do dispositivo, para ficar adequado ao jogo.

        textoPontuacao = new BitmapFont();
        textoPontuacao.setColor(Color.WHITE);
        textoPontuacao.getData().setScale(10);//aqui é onde é feito o Bitmap da fonte(mapeamento da fonte), e onde é definido a cor da pontuação do texto.

        textoReiniciar = new BitmapFont();
        textoReiniciar.setColor(Color.GREEN);
        textoReiniciar.getData().setScale(2);//feito o mapeamento do texto reiniciar, em cor verde.

        textoMelhorPontuacao = new BitmapFont();
        textoMelhorPontuacao.setColor(Color.RED);
        textoMelhorPontuacao.getData().setScale(2);//feito o mapeamento do texto melhor pontuação, em cor vermelha.

        shapeRenderer = new ShapeRenderer();
        circuloPassaro = new Circle();
        circuloMoeda = new Circle();
        retanguloCanoBaixo = new Rectangle();
        retanguloCanoCima = new Rectangle();//Aqui é definido o collider, o colisor.

        somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
        somColisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
        somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));
        moedaSom = Gdx.audio.newSound(Gdx.files.internal("som_moeda.wav"));//Aqui é onde é pego o som. Se for um arquivo .wav, ou se for outro tipo de arquivo.

        preferencias = Gdx.app.getPreferences("flappyBird");
        pontuacaoMaxima = preferencias.getInteger("pontuacaoMaxima", 0);//aqui é onde é salvo no dispositivo.

        camera = new OrthographicCamera();
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);//aqui é onde medido a posição da camera.
    }


    private void verificarEstadoJogo()//Aqui é onde é feito o mecanismo do jogo.
    {
        boolean toqueTela = Gdx.input.justTouched();//Aqui é onde é definido que com o toque da tela conseguimos nos locomover.

        if (estadoJogo == 0)
        {
            if (toqueTela)
            {
                gravidade = -15;
                estadoJogo = 1;
                somVoando.play();
            }
        }
        else if (estadoJogo == 1)
        {
            if (toqueTela)
            {
                gravidade = -15;
                somVoando.play();
            }

            posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 200;//posição do cano
            posicaoMoedaHorizontal -=Gdx.graphics.getDeltaTime() * 200;

            if (posicaoCanoHorizontal < -canoTopo.getWidth())
            {
                posicaoCanoHorizontal = larguraDispositivo;
                posicaoCanoVertical = random.nextInt(400) - 200;
                passouCano = false;//se o cano sai da tela, ele reseta a posição e pega uma altura aleatoria.
            }

            if(posicaoMoedaHorizontal < -moedaAtual.getWidth()*scaleMoeda /2)
            {
                resetaMoeda();
            }

            if (posicaoInicialVerticalPassaro > 0 || toqueTela) posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;//faz o passaro pular.

            gravidade++;//é a gravidade onde faz o passaro cair. 
        }
        else if (estadoJogo == 2)
        {
            if (pontos > pontuacaoMaxima)
            {
                pontuacaoMaxima = pontos;
                preferencias.putInteger("pontuacaoMaxima", pontuacaoMaxima);
                preferencias.flush();//Salva a pontuação do jogo.
            }

            posicaoHorizontalPassaro -= Gdx.graphics.getDeltaTime() * 500;

            if (toqueTela)
            {
                estadoJogo = 0;
                pontos = 0;
                gravidade = 0;
                posicaoHorizontalPassaro = 0;
                posicaoInicialVerticalPassaro = alturaDispositivo / 2;
                posicaoCanoHorizontal = larguraDispositivo;
                resetaMoeda();//Aqui é a tela inicial do jogo, onde é mostrado o passaro parado, sem pontuação.
            }
        }
    }

    private void resetaMoeda()
    {
         posicaoMoedaHorizontal = posicaoCanoHorizontal + larguraDispositivo/2;
         posicaoMoedaVertical = alturaDispositivo/2;

         if(random.nextInt(99)<=29) moedaAtual = moedaOuro;
         else moedaAtual =moedaPrata;
    }


    private void detectarColisoes()//detecta as colisões.
    {
        circuloPassaro.set
        (
            50 + posicaoHorizontalPassaro + passaros[0].getWidth() * scalePassaro / 2,
            posicaoInicialVerticalPassaro + passaros[0].getHeight() * scalePassaro / 2,
            passaros[0].getWidth() * scalePassaro / 2  //é definido a escala do passaro, a posição dele.
        );

        circuloMoeda.set
        (
          posicaoMoedaHorizontal,
          posicaoMoedaVertical,
          moedaAtual.getWidth()*scaleMoeda /2
        );

        retanguloCanoBaixo.set(
                posicaoCanoHorizontal,
                alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical,
                canoBaixo.getWidth(), canoBaixo.getHeight() //definição da posicão do cano debaixo, a altura, a largura.

        );
        retanguloCanoCima.set(
                posicaoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical,
                canoTopo.getWidth(), canoTopo.getHeight() //definição da posição do cano de cima, a altura e a largura. 

        );

        boolean colidiuCanoCima = Intersector.overlaps(circuloPassaro, retanguloCanoCima);
        boolean colidiuCanoBaixo = Intersector.overlaps(circuloPassaro, retanguloCanoBaixo);
        boolean colidiuMoeda = Intersector.overlaps(circuloPassaro, circuloMoeda); //O intersector.overlaps vai verificar a colisão, quando se passa o passaro entre os canos.

        if (colidiuCanoCima == true || colidiuCanoBaixo == true)
        {
            if (estadoJogo == 1)
            {
                somColisao.play();
                estadoJogo = 2;
            }
        } //se colidiu com o cano em estado do jogo um, é tocado o som de colisão e muda o estado pra 2.
        if(colidiuMoeda == true)
        {
            if(moedaAtual == moedaOuro) pontos += 10;
            else pontos += 5;
            moedaSom.play();

            posicaoMoedaVertical = alturaDispositivo * 2;
        }
    }

    private void desenharTexturas()
    {
        batch.setProjectionMatrix(camera.combined);//posicionamento da camera.
        batch.begin();//inicia os desenhos.

        batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo);

        if(estadoJogo == 0)
        {
            batch.draw
            (
                titulo,
                larguraDispositivo / 2,
                alturaDispositivo / 2
            );
        } //Desenhado o titulo no estado 0 do jogo.

        batch.draw
        (
            passaros[(int) variacao],
            50 + posicaoHorizontalPassaro,
            posicaoInicialVerticalPassaro,
            passaros[(int) variacao].getWidth() * scalePassaro,
            passaros[(int) variacao].getHeight() * scalePassaro
        );

        batch.draw
        (
           moedaAtual,
            posicaoMoedaHorizontal,
            posicaoMoedaVertical,
            moedaAtual.getWidth() * scaleMoeda,
            moedaAtual.getHeight()* scaleMoeda
        );

        batch.draw
        (
            canoBaixo,
            posicaoCanoHorizontal,
            alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical
        );

        batch.draw
        (
            canoTopo,
            posicaoCanoHorizontal,
            alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical
        );

        textoPontuacao.draw(batch, String.valueOf(pontos), larguraDispositivo / 2, alturaDispositivo - 110);//Aqui é onde desenhado o titulo, os canos, a moeda do jogo.


        if (estadoJogo == 2)
        {
            batch.draw
            (
                gameOver,
                larguraDispositivo / 2 - gameOver.getWidth() / 2,
                alturaDispositivo / 2
            );

            textoReiniciar.draw
            (
                batch,
                "Toque para Reiniciar!",
                larguraDispositivo / 2 - 140,
                alturaDispositivo / 2 - gameOver.getHeight() / 2
            );

            textoMelhorPontuacao.draw
            (
                batch,
                "Seu record é: " + pontuacaoMaxima + "pontos",
                larguraDispositivo / 2 - 140,
                alturaDispositivo / 2 - gameOver.getHeight()
            );
        }

        batch.end();//reune as texturas e renderiza elas
    }

	public void validarPontos()
    {
    	if(posicaoCanoHorizontal < 50-passaros[0].getWidth())
    	{
    		if(!passouCano)
    		{
    			pontos++;
    			passouCano = true;
    			somPontuacao.play();
			}
		}//Quando passar pelo cano, será tocado a musica e irá somar a pontuação. 

    	variacao += Gdx.graphics.getDeltaTime() *10;

    	if(variacao>3) variacao = 0;
	}//reseta as variações dos passaros(a quantidade de passaros que é feito a animação).

    @Override
    public void resize(int width, int height) { viewport.update(width, height); }//ele vai redimensiona a viewport com base na resolução do dispositivo. 

    @Override
    public void dispose() { }
}
