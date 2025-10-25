-- =========================================================
--  CUPCAKE DATABASE INITIALIZATION & TEST DATA SCRIPT
-- =========================================================

BEGIN;

-- =========================================================
-- TABLE CREATION
-- =========================================================

CREATE TABLE IF NOT EXISTS public.zip_codes
(
    zip_code integer NOT NULL,
    city character varying NOT NULL,
    PRIMARY KEY (zip_code)
    );

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
    PRIMARY KEY (user_id),
    UNIQUE (email),
    FOREIGN KEY (zip_code) REFERENCES zip_codes(zip_code)
    ON DELETE NO ACTION ON UPDATE CASCADE
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

CREATE TABLE IF NOT EXISTS public.bottoms
(
    bottom_id serial NOT NULL,
    bottom_flavour character varying NOT NULL,
    bottom_price double precision NOT NULL,
    PRIMARY KEY (bottom_id)
    );

CREATE TABLE IF NOT EXISTS public.toppings
(
    topping_id serial NOT NULL,
    topping_flavour character varying NOT NULL,
    topping_price double precision NOT NULL,
    PRIMARY KEY (topping_id),
    CONSTRAINT topping_flavour_unique UNIQUE (topping_flavour)
    );

CREATE TABLE IF NOT EXISTS public.orderlines
(
    orderline_id serial NOT NULL,
    order_id integer NOT NULL,
    topping_id integer NOT NULL,
    bottom_id integer NOT NULL,
    quantity integer NOT NULL,
    orderline_price double precision NOT NULL,
    PRIMARY KEY (orderline_id),
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (topping_id) REFERENCES toppings(topping_id)
    ON DELETE NO ACTION ON UPDATE CASCADE,
    FOREIGN KEY (bottom_id) REFERENCES bottoms(bottom_id)
    ON DELETE NO ACTION ON UPDATE CASCADE
    );

CREATE TABLE IF NOT EXISTS public.users_orders
(
    user_id integer NOT NULL,
    order_id integer NOT NULL,
    PRIMARY KEY (user_id, order_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
    ON DELETE CASCADE ON UPDATE CASCADE
    );

-- =========================================================
-- DATA POPULATION
-- =========================================================

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

INSERT INTO public.bottoms (bottom_flavour, bottom_price) VALUES
                                                              ('Chocolate', 5.00),
                                                              ('Vanilla', 5.00),
                                                              ('Nutmeg', 5.00),
                                                              ('Pistacio', 6.00),
                                                              ('Almond', 7.00)
    ON CONFLICT DO NOTHING;

INSERT INTO public.toppings (topping_flavour, topping_price) VALUES
                                                                 ('Chocolate', 5.00),
                                                                 ('Blueberry', 5.00),
                                                                 ('Raspberry', 5.00),
                                                                 ('Crispy', 6.00),
                                                                 ('Strawberry', 6.00),
                                                                 ('Rum/Raisin', 7.00),
                                                                 ('Orange', 8.00),
                                                                 ('Lemon', 8.00),
                                                                 ('Blue cheese', 9.00)
    ON CONFLICT DO NOTHING;

INSERT INTO public.users (firstname, lastname, email, password, phonenumber, street, zip_code, balance, admin) VALUES
                                                                                                                   ('System', 'Administrator', 'Admin@mail.dk', '1234', NULL, 'Head Office', 1000, 0, TRUE),
                                                                                                                   ('Poul', 'Hansen', 'poul.hansen@mail.dk', 'bornholm123', '20481234', 'Snellemark 14', 3700, 250.75, FALSE),
                                                                                                                   ('Maja', 'Christiansen', 'maja.christiansen@mail.dk', 'solskinsø', '30487766', 'Søndergade 8', 3740, 180.00, FALSE)
    ON CONFLICT (email) DO NOTHING;

INSERT INTO public.orders (order_date, pickup_date, paid, price_total) VALUES
                                                                           (now(), now() + interval '2 day', TRUE, 22.00),
                                                                           (now(), now() + interval '3 day', FALSE, 12.00),
                                                                           (now(), now() + interval '1 day', TRUE, 27.00);

INSERT INTO public.orderlines (order_id, topping_id, bottom_id, quantity, orderline_price) VALUES
                                                                              (1, 1, 1, 1, 10.00),
                                                                              (1, 2, 2, 1, 10.00),
                                                                              (2, 5, 4, 2, 24.00),
                                                                              (3, 6, 5, 1, 14.00),
                                                                              (3, 3, 1, 1, 10.00),
                                                                              (3, 9, 2, 1, 14.00);

INSERT INTO public.users_orders (user_id, order_id) VALUES
                                                        (2, 1),
                                                        (3, 2),
                                                        (3, 3);

CREATE SCHEMA IF NOT EXISTS test;

COMMIT;
