Il codice sorgente dell'applicazione si suddivide in tre parti:

- "lib" contiene la libreria fornita dall'universit�. Mette a disposizione delle API per la connessione tra l'applicazione android e il robot EV3 e per usufruire delle principali funzionalit� dei sensori/motori;

- "code" contiene tutte le Activity dell'app, inclusi i vari popup. Si compone delle parti generali: � definita l'implementazione dei pulsanti delle varie Activity e sono richiamati metodi che gestiscono tutte le funzionalit� in maniera pi� specifica; 

- "ourUtil" contiene tutti metodi e le classi di cui ci serviamo per implementare le funzionalit� sia dell'applicazione che del robot. Sono definiti i movimenti del robot, le parti grafiche pi� complesse, la gestione dei possibili errori, ecc...

Tra le risorse del progetto si trovano i layout delle varie activity e le immagini utilizzate per la grafica dell'applicazione. 