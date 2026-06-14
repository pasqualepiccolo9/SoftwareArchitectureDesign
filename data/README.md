# Persistenza dati

La persistenza dell'applicazione usa la cartella `data/`.

## `library.json`

Il file `data/library.json` contiene i dati persistenti dell'app:

```json
{
  "version": 1,
  "tracks": [
    {
      "id": 1,
      "title": "Titolo",
      "author": "Artista",
      "genre": "Genere",
      "year": 2026,
      "duration": 180,
      "filePath": "data/audio/brano.mp3"
    }
  ],
  "playlists": [
    {
      "name": "Preferiti",
      "trackIds": [1]
    }
  ]
}
```

Le playlist non duplicano i dati delle tracce: salvano solo gli id in `trackIds`.
Durante il caricamento gli id vengono risolti sulle tracce della libreria, cosi'
le relazioni runtime tra `Track` e `Playlist` restano coerenti.

## `audio/`

La cartella `data/audio/` e' pensata per contenere i file audio condivisi dal
team. I path dei file dentro al progetto vengono salvati in forma relativa, per
esempio `data/audio/brano.mp3`.
