CREATE TABLE spotify_user (
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

CREATE SEQUENCE spotify_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE music.spotify_user_id_seq OWNER TO postgres;

--
-- Name: spotify_user_id_seq; Type: SEQUENCE OWNED BY; Schema: music; Owner: postgres
--

ALTER SEQUENCE spotify_user_id_seq OWNED BY spotify_user.id;


--
-- Name: id; Type: DEFAULT; Schema: music; Owner: postgres
--

ALTER TABLE ONLY spotify_user ALTER COLUMN id SET DEFAULT nextval('spotify_user_id_seq'::regclass);


--
-- Name: spotify_user_pkey; Type: CONSTRAINT; Schema: music; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY spotify_user
    ADD CONSTRAINT spotify_user_pkey PRIMARY KEY (id);


--
-- PostgreSQL database dump complete
--
