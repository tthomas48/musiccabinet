-- adding spotify uris to various tables
alter table library.file_headertag_import add column spotify_uri varchar(100);
alter table library.file_headertag_import add column spotify_artist_uri varchar(100);
alter table library.file_headertag_import add column spotify_album_uri varchar(100);
alter table music.artist add column spotify_uri varchar(100);
alter table music.album add column spotify_uri varchar(100);
alter table music.track add column spotify_uri varchar(100);

