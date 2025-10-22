-- =========================================================
--  CUPCAKE DATABASE INITIALIZATION SCRIPT
--  Generated from ERD (pgAdmin 4) + Data Population
--  (phonenumber reverted to integer, users_orders reverted
--   to serial columns with no primary key per request)
-- =========================================================

BEGIN;

-- =========================================================
-- TABLE CREATION
-- =========================================================

CREATE TABLE IF NOT EXISTS public.users
(
    user_id serial NOT NULL,
    firstname character varying,
    lastname character varying,
    email character varying NOT NULL,
    password character varying NOT NULL,
    phonenumber integer,
    street character varying,
    zip_code integer,
    balance double precision NOT NULL DEFAULT 0,
    admin boolean DEFAULT false,
    CONSTRAINT users_pkey PRIMARY KEY (user_id),
    CONSTRAINT email_unique UNIQUE (email)
    );

CREATE TABLE IF NOT EXISTS public.zipcodes
(
    zip_code integer NOT NULL,
    city character varying NOT NULL,
    PRIMARY KEY (zipcodes)
    );

CREATE TABLE IF NOT EXISTS public.orders
(
    order_id serial NOT NULL,
    order_date timestamp with time zone NOT NULL DEFAULT now(),
    pickup_date timestamp with time zone,
                              paid boolean NOT NULL DEFAULT false,
                              price_total double precision,
                              PRIMARY KEY (order_id)
    );

CREATE TABLE IF NOT EXISTS public.orderlines
(
    order_line_id serial NOT NULL,
    order_id integer NOT NULL,
    topping_id integer NOT NULL,
    bottom_id integer NOT NULL,
    quantity integer NOT NULL,
    PRIMARY KEY (order_line_id)
    );

CREATE TABLE IF NOT EXISTS public.bottoms
(
    bottom_id serial NOT NULL,
    flavour character varying NOT NULL,
    price double precision NOT NULL,
    PRIMARY KEY (bottom_id)
    );

CREATE TABLE IF NOT EXISTS public.toppings
(
    topping_id serial NOT NULL,
    flavour character varying NOT NULL,
    price double precision NOT NULL,
    PRIMARY KEY (topping_id)
    );

CREATE TABLE IF NOT EXISTS public.users_orders
(
    users_user_id serial NOT NULL,
    orders_order_id serial NOT NULL
);

-- =========================================================
-- FOREIGN KEYS
-- =========================================================

ALTER TABLE IF EXISTS public.users
    ADD CONSTRAINT zipcode_fk FOREIGN KEY (zipcode)
    REFERENCES public.zipcodes (zipcode) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE NO ACTION
    NOT VALID;

ALTER TABLE IF EXISTS public.orderlines
    ADD CONSTRAINT orders_fk FOREIGN KEY (order_id)
    REFERENCES public.orders (order_id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE NO ACTION
    NOT VALID;

ALTER TABLE IF EXISTS public.orderlines
    ADD CONSTRAINT toppings_fk FOREIGN KEY (topping_id)
    REFERENCES public.toppings (topping_id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE NO ACTION
    NOT VALID;

ALTER TABLE IF EXISTS public.orderlines
    ADD CONSTRAINT bottoms_fk FOREIGN KEY (bottom_id)
    REFERENCES public.bottoms (bottom_id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE NO ACTION
    NOT VALID;

ALTER TABLE IF EXISTS public.users_orders
    ADD FOREIGN KEY (users_user_id)
    REFERENCES public.users (user_id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE NO ACTION
    NOT VALID;

ALTER TABLE IF EXISTS public.users_orders
    ADD FOREIGN KEY (orders_order_id)
    REFERENCES public.orders (order_id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE NO ACTION
    NOT VALID;

-- =========================================================
-- DATA POPULATION
-- =========================================================

-- ---------------------------------------------
-- Zipcodes
-- ---------------------------------------------
INSERT INTO public.zipcodes (zipcode, city)
VALUES (1000, 'Copenhagen')
    ON CONFLICT DO NOTHING;

-- ---------------------------------------------
-- Bottoms
-- ---------------------------------------------
INSERT INTO public.bottoms (flavour, price) VALUES
                                                ('Chocolate', 5.00),
                                                ('Vanilla', 5.00),
                                                ('Nutmeg', 5.00),
                                                ('Pistacio', 6.00),
                                                ('Almond', 7.00)
    ON CONFLICT DO NOTHING;

-- ---------------------------------------------
-- Toppings
-- ---------------------------------------------
INSERT INTO public.toppings (flavour, price) VALUES
                                                 ('Chocolate', 5.00),
                                                 ('Blueberry', 5.00),
                                                 ('Rasberry', 5.00),
                                                 ('Crispy', 6.00),
                                                 ('Strawberry', 6.00),
                                                 ('Rum/Raisin', 7.00),
                                                 ('Orange', 8.00),
                                                 ('Lemon', 8.00),
                                                 ('Blue cheese', 9.00)
    ON CONFLICT DO NOTHING;

-- ---------------------------------------------
-- Admin User
-- ---------------------------------------------
INSERT INTO public.users (
    firstname,
    lastname,
    email,
    password,
    phonenumber,
    address,
    zipcode,
    balance,
    isadmin
) VALUES (
             'System',
             'Administrator',
             'Admin@mail.dk',
             '1234',        -- ⚠️ In production, store hashed password instead
             NULL,
             'Head Office',
             1000,
             0,
             TRUE
         )
    ON CONFLICT (email) DO NOTHING;

COMMIT;
