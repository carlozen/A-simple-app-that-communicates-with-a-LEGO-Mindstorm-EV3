package it.unive.dais.legodroid.code;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import it.unive.dais.legodroid.R;
public class RulesActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);
        //TODO: write how the map have to be built
        TextView textView = findViewById(R.id.rules_text);
        textView.setTextColor(Color.BLACK);
        textView.setText("Un Robot muove una serie di oggetti da una zona deposito a una serie di " +
                "stazioni collegate tra loro tramite delle linee guida  \n" +
                "Per muoversi autonomamente il robot utilizza le linee guida nere, mentre il verde " +
                "e il giallo indicano la presenza di una stazione nella quale depositare oggetti. " +
                "Il colore rosso è utilizzato per indicare la fine di una linea guida\n" +
                "\n" +
                "DUE MODALITÁ DI INTERAZIONE:\n" +
                "MANUALE: muovi il robot e utilizza il braccio per spostare gli oggetti dalla zona deposito alle stazioni \n" +
                "\n" +
                "AUTOMATICA: decidi la superficie di lavoro e lascia che sia il robot a fare il lavoro per te \n" +
                "\n" +
                "COME FUNZIONA:\n" +
                "MANUALE: usa le frecce direzionali per muovere il robot e i tasti azione per alzare o abbassare il braccio meccanico\n" +
                "\n" +
                "AUTOMATICA: scegli se far operare il robot autonomamente su una superficie di lavoro precedentemente memorizzata o se fargli memorizzare una nuova superficie. Identificata l’area di lavoro questa verrà visualizzate a schermo e l’utente dovrà indicare al robot la stazione in cui depositare l’oggetto; il robot porterà a termine l’operazione e la stazione appena utilizzata verrà segnata come occupata.");
    }
}
