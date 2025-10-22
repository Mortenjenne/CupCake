-- =========================================================
--  CUPCAKE DATABASE INITIALIZATION & TEST DATA SCRIPT
--  (Matches original ERD structure and includes sample data)
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
    admin boolean,
    CONSTRAINT zip PRIMARY KEY (user_id),
    CONSTRAINT email_unique UNIQUE (email)
    );

CREATE TABLE IF NOT EXISTS public.zip_codes
(
    zip_code integer NOT NULL,
    city character varying NOT NULL,
    PRIMARY KEY (zip_code)
    );

CREATE TABLE IF NOT EXISTS public.orders
(
    order_id serial NOT NULL,
    order_date timestamp with time zone NOT NULL DEFAULT now(),
    pickup_date timestamp with time zone,
                              paid boolean NOT NULL,
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
    ADD CONSTRAINT zip_code_fk FOREIGN KEY (zip_code)
    REFERENCES public.zip_codes (zip_code) MATCH SIMPLE
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
-- Zip_codes
-- ---------------------------------------------
INSERT INTO public.zip_codes (zip_code, city) VALUES
                                                (1000, 'København'),
                                                (2000, 'Frederiksberg'),
                                                (8000, 'Aarhus'),
                                                (5000, 'Odense'),
                                                (3700, 'Rønne'),
                                                (3720, 'Aakirkeby'),
                                                (3730, 'Nexø'),
                                                (3740, 'Svaneke'),
                                                (3751, 'Østermarie'),
                                                (3760, 'Gudhjem'),
                                                (3770, 'Allinge'),
                                                (3782, 'Klemensker'),
                                                (3790, 'Hasle')
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
-- Users (Admin + 2 Customers)
-- ---------------------------------------------
INSERT INTO public.users (
    firstname, lastname, email, password,
    phonenumber, street, zip_code, balance, admin
) VALUES
      ('System', 'Administrator', 'Admin@mail.dk', '1234', NULL, 'Head Office', 1000, 0, TRUE),
      ('Poul', 'Hansen', 'poul.hansen@mail.dk', 'bornholm123', 20481234, 'Snellemark 14', 3700, 250.75, FALSE),
      ('Maja', 'Christiansen', 'maja.christiansen@mail.dk', 'solskinsø', 30487766, 'Søndergade 8', 3740, 180.00, FALSE)
    ON CONFLICT (email) DO NOTHING;

-- ---------------------------------------------
-- Orders
-- ---------------------------------------------
INSERT INTO public.orders (order_date, pickup_date, paid, price_total)
VALUES
    (now(), now() + interval '2 day', TRUE, 22.00),
    (now(), now() + interval '3 day', FALSE, 12.00),
    (now(), now() + interval '1 day', TRUE, 27.00)
    ON CONFLICT DO NOTHING;

-- ---------------------------------------------
-- Orderlines
-- ---------------------------------------------
-- Order 1 (2 cupcakes, different combos)
INSERT INTO public.orderlines (order_id, topping_id, bottom_id, quantity)
VALUES
    (1, 1, 1, 1),  -- Chocolate topping + Chocolate bottom
    (1, 2, 2, 1);  -- Blueberry topping + Vanilla bottom

-- Order 2 (1 cupcake)
INSERT INTO public.orderlines (order_id, topping_id, bottom_id, quantity)
VALUES
    (2, 5, 4, 2);  -- Strawberry topping + Pistacio bottom x2

-- Order 3 (3 cupcakes)
INSERT INTO public.orderlines (order_id, topping_id, bottom_id, quantity)
VALUES
    (3, 6, 5, 1),  -- Rum/Raisin + Almond
    (3, 3, 1, 1),  -- Raspberry + Chocolate
    (3, 9, 2, 1);  -- Blue cheese + Vanilla

-- ---------------------------------------------
-- Users_Orders Mapping
-- ---------------------------------------------
INSERT INTO public.users_orders (users_user_id, orders_order_id)
VALUES
    (2, 1),  -- Poul Hansen made Order 1
    (3, 2),  -- Maja Christiansen made Order 2
    (3, 3);  -- Maja Smith Christiansen made Order 3

-- ==============================================
-- CREATE TEST SCHEMA (for integration/unit tests)
-- ==============================================
CREATE SCHEMA IF NOT EXISTS test;


COMMIT;
