CREATE OR REPLACE FUNCTION get_number_of_less_venue_tickets(min_venue INTEGER)
RETURNS INTEGER AS $$
DECLARE
    ticket_count INTEGER;
BEGIN
    SELECT COUNT(*)
    INTO ticket_count
    FROM tickets AS t
    WHERE t.venue_id < min_venue;
    RETURN ticket_count;
END;
$$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION get_number_of_more_number_tickets(max_number INTEGER)
RETURNS INTEGER AS $$
DECLARE
    ticket_count INTEGER;
BEGIN
    SELECT COUNT(*)
    INTO ticket_count
    FROM tickets AS t
    WHERE t.number > max_number;
    RETURN ticket_count;
END;
$$
LANGUAGE plpgsql;


CREATE OR REPLACE PROCEDURE copy_vip_ticket(ticket_id INTEGER)
AS $$
DECLARE
    original_ticket RECORD;
BEGIN
    SELECT * INTO original_ticket
    FROM tickets
    WHERE id = ticket_id;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Ticket with ID % not found', ticket_id;
    END IF;
    INSERT INTO tickets (
        name, coordinates_id, creation_date, person_id, event_id, price, type, discount, number, venue_id, user_id
    )
    VALUES (
        original_ticket.name,
        original_ticket.coordinates_id,
        original_ticket.creation_date,
        original_ticket.person_id,
        original_ticket.event_id,
        original_ticket.price * 2,
        'VIP',
        original_ticket.discount,
        original_ticket.number,
        original_ticket.venue_id,
        original_ticket.user_id
    );
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE PROCEDURE copy_discount_ticket(ticket_id INTEGER, discount_percent INTEGER)
AS $$
DECLARE
    original_ticket RECORD;
    new_price INTEGER;
BEGIN
    SELECT * INTO original_ticket
    FROM tickets
    WHERE id = ticket_id;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Ticket with ID % not found', ticket_id;
    END IF;
    new_price := original_ticket.price + (original_ticket.price * discount_percent / 100);
    INSERT INTO tickets (
        name, coordinates_id, creation_date, person_id, event_id, price, type, discount, number, venue_id, user_id
    )
    VALUES (
        original_ticket.name,
        original_ticket.coordinates_id,
        original_ticket.creation_date,
        original_ticket.person_id,
        original_ticket.event_id,
        new_price,
        original_ticket.type,
        discount_percent,
        original_ticket.number,
        original_ticket.venue_id,
        original_ticket.user_id
    );
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_tickets_with_greater_number(max_number INTEGER)
RETURNS SETOF tickets AS $$
BEGIN
    RETURN QUERY SELECT *
    FROM tickets AS t
    WHERE t.number > max_number;
END;
$$
LANGUAGE plpgsql;