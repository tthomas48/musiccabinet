CREATE TABLE music.spotify_user (
    id integer NOT NULL,
    username text NOT NULL,
    blob text,
    fullname text,
    displayname text,
    country text,
    imageurl text
);


ALTER TABLE music.spotify_user OWNER TO postgres;

--
-- Name: spotify_user_id_seq; Type: SEQUENCE; Schema: music; Owner: postgres
--

CREATE SEQUENCE music.spotify_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE music.spotify_user_id_seq OWNER TO postgres;

--
-- Name: spotify_user_id_seq; Type: SEQUENCE OWNED BY; Schema: music; Owner: postgres
--

ALTER SEQUENCE music.spotify_user_id_seq OWNED BY music.spotify_user.id;


--
-- Name: id; Type: DEFAULT; Schema: music; Owner: postgres
--

ALTER TABLE ONLY music.spotify_user ALTER COLUMN id SET DEFAULT nextval('music.spotify_user_id_seq'::regclass);


--
-- Name: spotify_user_pkey; Type: CONSTRAINT; Schema: music; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY music.spotify_user
    ADD CONSTRAINT spotify_user_pkey PRIMARY KEY (id);


--
-- PostgreSQL database dump complete
--
