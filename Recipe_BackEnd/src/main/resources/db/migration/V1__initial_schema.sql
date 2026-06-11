--
-- PostgreSQL database dump
--


-- Dumped from database version 15.16 (Debian 15.16-1.pgdg13+1)
-- Dumped by pg_dump version 15.16 (Debian 15.16-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
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
-- Name: courses; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.courses (
    id bigint NOT NULL,
    name character varying(255) NOT NULL
);


--
-- Name: courses_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.courses_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: courses_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.courses_id_seq OWNED BY public.courses.id;


--
-- Name: cuisines; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cuisines (
    id bigint NOT NULL,
    name character varying(255) NOT NULL
);


--
-- Name: cuisines_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.cuisines_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: cuisines_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.cuisines_id_seq OWNED BY public.cuisines.id;


--
-- Name: diet_types; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.diet_types (
    id bigint NOT NULL,
    name character varying(255) NOT NULL
);


--
-- Name: diet_types_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.diet_types_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: diet_types_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.diet_types_id_seq OWNED BY public.diet_types.id;


--
-- Name: flavours; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.flavours (
    id bigint NOT NULL,
    name character varying(255) NOT NULL
);


--
-- Name: flavours_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.flavours_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: flavours_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.flavours_id_seq OWNED BY public.flavours.id;


--
-- Name: ingredients; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ingredients (
    id bigint NOT NULL,
    name character varying(255) NOT NULL
);


--
-- Name: ingredients_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ingredients_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ingredients_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.ingredients_id_seq OWNED BY public.ingredients.id;


--
-- Name: recipe_courses; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.recipe_courses (
    course_id bigint NOT NULL,
    id bigint NOT NULL,
    recipe_id bigint NOT NULL
);


--
-- Name: recipe_courses_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.recipe_courses_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: recipe_courses_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.recipe_courses_id_seq OWNED BY public.recipe_courses.id;


--
-- Name: recipe_cuisines; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.recipe_cuisines (
    cuisine_id bigint NOT NULL,
    id bigint NOT NULL,
    recipe_id bigint NOT NULL
);


--
-- Name: recipe_cuisines_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.recipe_cuisines_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: recipe_cuisines_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.recipe_cuisines_id_seq OWNED BY public.recipe_cuisines.id;


--
-- Name: recipe_diet_types; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.recipe_diet_types (
    diet_type_id bigint NOT NULL,
    id bigint NOT NULL,
    recipe_id bigint NOT NULL
);


--
-- Name: recipe_diet_types_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.recipe_diet_types_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: recipe_diet_types_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.recipe_diet_types_id_seq OWNED BY public.recipe_diet_types.id;


--
-- Name: recipe_flavours; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.recipe_flavours (
    flavour_id bigint NOT NULL,
    id bigint NOT NULL,
    recipe_id bigint NOT NULL
);


--
-- Name: recipe_flavours_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.recipe_flavours_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: recipe_flavours_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.recipe_flavours_id_seq OWNED BY public.recipe_flavours.id;


--
-- Name: recipe_ingredients; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.recipe_ingredients (
    id bigint NOT NULL,
    ingredient_id bigint,
    recipe_id bigint,
    note character varying(50),
    amount character varying(255) NOT NULL
);


--
-- Name: recipe_ingredients_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.recipe_ingredients_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: recipe_ingredients_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.recipe_ingredients_id_seq OWNED BY public.recipe_ingredients.id;


--
-- Name: recipe_ratings; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.recipe_ratings (
    score double precision NOT NULL,
    created_at timestamp(6) without time zone,
    id bigint NOT NULL,
    recipe_id bigint NOT NULL,
    user_id bigint NOT NULL
);


--
-- Name: recipe_ratings_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.recipe_ratings_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: recipe_ratings_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.recipe_ratings_id_seq OWNED BY public.recipe_ratings.id;


--
-- Name: recipe_search_sync_events; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.recipe_search_sync_events (
    id bigint NOT NULL,
    attempts integer NOT NULL,
    created_at timestamp(6) without time zone,
    last_error text,
    next_retry_at timestamp(6) without time zone NOT NULL,
    operation character varying(20) NOT NULL,
    recipe_id bigint NOT NULL,
    status character varying(20) NOT NULL,
    updated_at timestamp(6) without time zone,
    CONSTRAINT recipe_search_sync_events_operation_check CHECK (((operation)::text = ANY ((ARRAY['UPSERT'::character varying, 'DELETE'::character varying])::text[]))),
    CONSTRAINT recipe_search_sync_events_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'DONE'::character varying, 'FAILED'::character varying])::text[])))
);


--
-- Name: recipe_search_sync_events_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.recipe_search_sync_events_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: recipe_search_sync_events_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.recipe_search_sync_events_id_seq OWNED BY public.recipe_search_sync_events.id;


--
-- Name: recipe_steps; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.recipe_steps (
    step_number integer NOT NULL,
    id bigint NOT NULL,
    recipe_id bigint NOT NULL,
    content text NOT NULL,
    image_urls character varying[]
);


--
-- Name: recipe_steps_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.recipe_steps_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: recipe_steps_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.recipe_steps_id_seq OWNED BY public.recipe_steps.id;


--
-- Name: recipes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.recipes (
    average_rating double precision,
    cooking_time_min integer NOT NULL,
    rating_count integer,
    author_id bigint,
    created_at timestamp(6) without time zone,
    id bigint NOT NULL,
    updated_at timestamp(6) without time zone,
    difficulty character varying(20),
    title character varying(100) NOT NULL,
    cover_image character varying(255) NOT NULL,
    description text,
    version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT recipes_difficulty_check CHECK (((difficulty)::text = ANY ((ARRAY['EASY'::character varying, 'MEDIUM'::character varying, 'HARD'::character varying])::text[])))
);


--
-- Name: recipes_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.recipes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: recipes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.recipes_id_seq OWNED BY public.recipes.id;


--
-- Name: user_saved_recipes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_saved_recipes (
    created_at timestamp(6) without time zone,
    id bigint NOT NULL,
    recipe_id bigint NOT NULL,
    user_id bigint NOT NULL
);


--
-- Name: user_saved_recipes_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.user_saved_recipes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_saved_recipes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.user_saved_recipes_id_seq OWNED BY public.user_saved_recipes.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    created_at timestamp(6) without time zone,
    id bigint NOT NULL,
    username character varying(50) NOT NULL,
    email character varying(100),
    avatar_url character varying(255),
    bio character varying(255),
    password_hash character varying(255) NOT NULL,
    role character varying(255),
    preferences jsonb,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['ADMIN'::character varying, 'USER'::character varying])::text[])))
);


--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: courses id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.courses ALTER COLUMN id SET DEFAULT nextval('public.courses_id_seq'::regclass);


--
-- Name: cuisines id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cuisines ALTER COLUMN id SET DEFAULT nextval('public.cuisines_id_seq'::regclass);


--
-- Name: diet_types id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.diet_types ALTER COLUMN id SET DEFAULT nextval('public.diet_types_id_seq'::regclass);


--
-- Name: flavours id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.flavours ALTER COLUMN id SET DEFAULT nextval('public.flavours_id_seq'::regclass);


--
-- Name: ingredients id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ingredients ALTER COLUMN id SET DEFAULT nextval('public.ingredients_id_seq'::regclass);


--
-- Name: recipe_courses id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_courses ALTER COLUMN id SET DEFAULT nextval('public.recipe_courses_id_seq'::regclass);


--
-- Name: recipe_cuisines id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_cuisines ALTER COLUMN id SET DEFAULT nextval('public.recipe_cuisines_id_seq'::regclass);


--
-- Name: recipe_diet_types id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_diet_types ALTER COLUMN id SET DEFAULT nextval('public.recipe_diet_types_id_seq'::regclass);


--
-- Name: recipe_flavours id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_flavours ALTER COLUMN id SET DEFAULT nextval('public.recipe_flavours_id_seq'::regclass);


--
-- Name: recipe_ingredients id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_ingredients ALTER COLUMN id SET DEFAULT nextval('public.recipe_ingredients_id_seq'::regclass);


--
-- Name: recipe_ratings id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_ratings ALTER COLUMN id SET DEFAULT nextval('public.recipe_ratings_id_seq'::regclass);


--
-- Name: recipe_search_sync_events id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_search_sync_events ALTER COLUMN id SET DEFAULT nextval('public.recipe_search_sync_events_id_seq'::regclass);


--
-- Name: recipe_steps id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_steps ALTER COLUMN id SET DEFAULT nextval('public.recipe_steps_id_seq'::regclass);


--
-- Name: recipes id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipes ALTER COLUMN id SET DEFAULT nextval('public.recipes_id_seq'::regclass);


--
-- Name: user_saved_recipes id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_saved_recipes ALTER COLUMN id SET DEFAULT nextval('public.user_saved_recipes_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Name: courses courses_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT courses_name_key UNIQUE (name);


--
-- Name: courses courses_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT courses_pkey PRIMARY KEY (id);


--
-- Name: cuisines cuisines_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cuisines
    ADD CONSTRAINT cuisines_name_key UNIQUE (name);


--
-- Name: cuisines cuisines_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cuisines
    ADD CONSTRAINT cuisines_pkey PRIMARY KEY (id);


--
-- Name: diet_types diet_types_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.diet_types
    ADD CONSTRAINT diet_types_name_key UNIQUE (name);


--
-- Name: diet_types diet_types_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.diet_types
    ADD CONSTRAINT diet_types_pkey PRIMARY KEY (id);


--
-- Name: flavours flavours_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.flavours
    ADD CONSTRAINT flavours_name_key UNIQUE (name);


--
-- Name: flavours flavours_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.flavours
    ADD CONSTRAINT flavours_pkey PRIMARY KEY (id);


--
-- Name: ingredients ingredients_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ingredients
    ADD CONSTRAINT ingredients_name_key UNIQUE (name);


--
-- Name: ingredients ingredients_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ingredients
    ADD CONSTRAINT ingredients_pkey PRIMARY KEY (id);


--
-- Name: recipe_courses recipe_courses_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_courses
    ADD CONSTRAINT recipe_courses_pkey PRIMARY KEY (id);


--
-- Name: recipe_cuisines recipe_cuisines_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_cuisines
    ADD CONSTRAINT recipe_cuisines_pkey PRIMARY KEY (id);


--
-- Name: recipe_diet_types recipe_diet_types_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_diet_types
    ADD CONSTRAINT recipe_diet_types_pkey PRIMARY KEY (id);


--
-- Name: recipe_flavours recipe_flavours_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_flavours
    ADD CONSTRAINT recipe_flavours_pkey PRIMARY KEY (id);


--
-- Name: recipe_ingredients recipe_ingredients_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_ingredients
    ADD CONSTRAINT recipe_ingredients_pkey PRIMARY KEY (id);


--
-- Name: recipe_ratings recipe_ratings_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_ratings
    ADD CONSTRAINT recipe_ratings_pkey PRIMARY KEY (id);


--
-- Name: recipe_ratings recipe_ratings_user_id_recipe_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_ratings
    ADD CONSTRAINT recipe_ratings_user_id_recipe_id_key UNIQUE (user_id, recipe_id);


--
-- Name: recipe_search_sync_events recipe_search_sync_events_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_search_sync_events
    ADD CONSTRAINT recipe_search_sync_events_pkey PRIMARY KEY (id);


--
-- Name: recipe_steps recipe_steps_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_steps
    ADD CONSTRAINT recipe_steps_pkey PRIMARY KEY (id);


--
-- Name: recipes recipes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipes
    ADD CONSTRAINT recipes_pkey PRIMARY KEY (id);


--
-- Name: user_saved_recipes uk_user_saved_user_recipe; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_saved_recipes
    ADD CONSTRAINT uk_user_saved_user_recipe UNIQUE (user_id, recipe_id);


--
-- Name: recipe_ratings uke5mthmuw2ur5pa5alug7u8lny; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_ratings
    ADD CONSTRAINT uke5mthmuw2ur5pa5alug7u8lny UNIQUE (user_id, recipe_id);


--
-- Name: user_saved_recipes user_saved_recipes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_saved_recipes
    ADD CONSTRAINT user_saved_recipes_pkey PRIMARY KEY (id);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- Name: user_saved_recipes fk5rfntjwtmd4qy1s1bifd9p1bd; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_saved_recipes
    ADD CONSTRAINT fk5rfntjwtmd4qy1s1bifd9p1bd FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: recipe_courses fk8m862q7yw9qg2xjnmk4uvh5yw; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_courses
    ADD CONSTRAINT fk8m862q7yw9qg2xjnmk4uvh5yw FOREIGN KEY (recipe_id) REFERENCES public.recipes(id);


--
-- Name: recipe_cuisines fk8p2bl42laest33wk76a5hjnq6; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_cuisines
    ADD CONSTRAINT fk8p2bl42laest33wk76a5hjnq6 FOREIGN KEY (recipe_id) REFERENCES public.recipes(id);


--
-- Name: recipe_flavours fk9hhw0mpfuvcrsm3rosl36vdqq; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_flavours
    ADD CONSTRAINT fk9hhw0mpfuvcrsm3rosl36vdqq FOREIGN KEY (recipe_id) REFERENCES public.recipes(id);


--
-- Name: recipe_diet_types fkbsxmbf0m2lucxvwi95kwvs534; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_diet_types
    ADD CONSTRAINT fkbsxmbf0m2lucxvwi95kwvs534 FOREIGN KEY (diet_type_id) REFERENCES public.diet_types(id);


--
-- Name: recipe_ingredients fkcqlw8sor5ut10xsuj3jnttkc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_ingredients
    ADD CONSTRAINT fkcqlw8sor5ut10xsuj3jnttkc FOREIGN KEY (recipe_id) REFERENCES public.recipes(id);


--
-- Name: recipe_diet_types fkf95c3tkl41bohkunqbwn6v279; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_diet_types
    ADD CONSTRAINT fkf95c3tkl41bohkunqbwn6v279 FOREIGN KEY (recipe_id) REFERENCES public.recipes(id);


--
-- Name: user_saved_recipes fkfm7marif8ukb5y1qpndvy3mvn; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_saved_recipes
    ADD CONSTRAINT fkfm7marif8ukb5y1qpndvy3mvn FOREIGN KEY (recipe_id) REFERENCES public.recipes(id);


--
-- Name: recipe_ingredients fkgukrw6na9f61kb8djkkuvyxy8; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_ingredients
    ADD CONSTRAINT fkgukrw6na9f61kb8djkkuvyxy8 FOREIGN KEY (ingredient_id) REFERENCES public.ingredients(id);


--
-- Name: recipes fkhcd6j9baovkdrh8gu9my8lco5; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipes
    ADD CONSTRAINT fkhcd6j9baovkdrh8gu9my8lco5 FOREIGN KEY (author_id) REFERENCES public.users(id);


--
-- Name: recipe_courses fkikjl7pghyxd64oha6ihm6f6k2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_courses
    ADD CONSTRAINT fkikjl7pghyxd64oha6ihm6f6k2 FOREIGN KEY (course_id) REFERENCES public.courses(id);


--
-- Name: recipe_steps fkof4i3g3aiwgro5ykaf1j28iw1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_steps
    ADD CONSTRAINT fkof4i3g3aiwgro5ykaf1j28iw1 FOREIGN KEY (recipe_id) REFERENCES public.recipes(id);


--
-- Name: recipe_flavours fkoku2o657qnrdbqk8pmyh9jtsm; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_flavours
    ADD CONSTRAINT fkoku2o657qnrdbqk8pmyh9jtsm FOREIGN KEY (flavour_id) REFERENCES public.flavours(id);


--
-- Name: recipe_cuisines fkrj2sqqlo7mgyx984704e77d23; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recipe_cuisines
    ADD CONSTRAINT fkrj2sqqlo7mgyx984704e77d23 FOREIGN KEY (cuisine_id) REFERENCES public.cuisines(id);


--
-- PostgreSQL database dump complete
--


