--
-- PostgreSQL database dump
--

\restrict WvbbHjgM8OzCzn3wK8om4BQewNgZjqBXR3TL1osCcIuiKOVH7fH2HH3RB3iJbfw

-- Dumped from database version 18.6
-- Dumped by pg_dump version 18.6

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: material; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.material (
    id integer NOT NULL,
    nome character varying(120) NOT NULL,
    quantidade integer NOT NULL,
    descricao text DEFAULT ''::text NOT NULL,
    CONSTRAINT material_nome_check CHECK ((length(TRIM(BOTH FROM nome)) > 0)),
    CONSTRAINT material_quantidade_check CHECK ((quantidade > 0))
);


--
-- Name: material_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.material ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.material_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Data for Name: material; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.material (id, nome, quantidade, descricao) FROM stdin;
1	Tijolo (exemplo)	20	Registro de exemplo para apresentar o CRUD
\.


--
-- Name: material_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.material_id_seq', 1, true);


--
-- Name: material material_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.material
    ADD CONSTRAINT material_pkey PRIMARY KEY (id);


--
-- PostgreSQL database dump complete
--

\unrestrict WvbbHjgM8OzCzn3wK8om4BQewNgZjqBXR3TL1osCcIuiKOVH7fH2HH3RB3iJbfw

