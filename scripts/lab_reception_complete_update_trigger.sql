DROP TRIGGER IF EXISTS `lab_reception_complete_update_trigger`;

DELIMITER $$
 
create trigger lab_reception_complete_update_trigger
after update
on redcap_data for each row
begin

	declare lab_report smallint(4);
	declare lab_receiving smallint(4);
	declare lab_extraction smallint(4);
	declare lab_results smallint(4);
	declare received_datetime varchar(100);
	declare receiving_personnel varchar(100);
	declare receiving_lab varchar(100);
	declare report_max_event smallint(4);
	declare c_project_id int(10);
	declare c_event_id int(10);
	declare c_record varchar(100);
	declare c_field_name varchar(100);
	declare c_value text;
	declare c_instance smallint(4);
	declare pos varchar(2);
	DECLARE done INT DEFAULT FALSE;
	
	declare specimen cursor for
	select project_id, event_id, record, field_name, value, instance
	from redcap_data
	where project_id = new.project_id
		and event_id = new.event_id
		and record = new.record
		and field_name like 'lab_rec_barcode_%';
    
	DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
	select 352 into lab_report;
	select 362 into lab_receiving;
	select 344 into lab_extraction;
	select 345 into lab_results;
    
    -- Stop calling this procedure with any other data
	if (new.field_name = 'sarscov2_lab_reception_complete' and new.value = '2' and new.project_id = lab_receiving) then
        
		select max(event_id) 
		into report_max_event 
		from redcap_data 
		where project_id = lab_report;
					
		-- -----------------------------------------------------------------
		-- Find receiving date time
		-- -----------------------------------------------------------------
		select value 
		into received_datetime
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'received_datetime';
					
		-- -----------------------------------------------------------------
		-- Find Receiving personnel
		-- -----------------------------------------------------------------
		select value into receiving_personnel
		from redcap_data 
			where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'receiving_personnel';
				
		-- -----------------------------------------------------------------
		-- Find receiving lab
		-- -----------------------------------------------------------------
		select value 
		into receiving_lab
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'receiving_lab';
			
		-- ------------------------------------------------------------------
		-- Go through the specimen in the batch by looking for the 
		-- barcodes.
		-- ------------------------------------------------------------------
				
		open specimen;
		s1_loop: LOOP
			fetch specimen into c_project_id, c_event_id, c_record, c_field_name, c_value, c_instance;
			IF done THEN
				LEAVE s1_loop;
			END IF;
							
			-- Update the staging area
			update redcap_ddp_specimen 
			set receiving_personnel = receiving_personnel,
				receiving_date_time = received_datetime,
				RECEIVING_LAB = receiving_lab,
				dhis2_synched = false
			where specimen_barcode = c_value;
			
		END LOOP s1_loop;
		
	end if;
end; $$

DELIMITER ;

DROP TRIGGER IF EXISTS `lab_reception_complete_insert_trigger`;

DELIMITER $$
 
create trigger lab_reception_complete_insert_trigger
after insert
on redcap_data for each row
begin

	declare lab_report smallint(4);
	declare lab_receiving smallint(4);
	declare lab_extraction smallint(4);
	declare lab_results smallint(4);
	declare received_datetime varchar(100);
	declare receiving_personnel varchar(100);
	declare receiving_lab varchar(100);
	declare report_max_event smallint(4);
	declare c_project_id int(10);
	declare c_event_id int(10);
	declare c_record varchar(100);
	declare c_field_name varchar(100);
	declare c_value text;
	declare c_instance smallint(4);
	declare pos varchar(2);
	DECLARE done INT DEFAULT FALSE;
	
	declare specimen cursor for
	select project_id, event_id, record, field_name, value, instance
	from redcap_data
	where project_id = new.project_id
		and event_id = new.event_id
		and record = new.record
		and field_name like 'lab_rec_barcode_%';
    
	DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
	select 352 into lab_report;
	select 362 into lab_receiving;
	select 344 into lab_extraction;
	select 345 into lab_results;
    
    -- Stop calling this procedure with any other data
	if (new.field_name = 'sarscov2_lab_reception_complete' and new.value = '2' and new.project_id = lab_receiving) then
        
		select max(event_id) 
		into report_max_event 
		from redcap_data 
		where project_id = lab_report;
					
		-- -----------------------------------------------------------------
		-- Find receiving date time
		-- -----------------------------------------------------------------
		select value 
		into received_datetime
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'received_datetime';
					
		-- -----------------------------------------------------------------
		-- Find Receiving personnel
		-- -----------------------------------------------------------------
		select value into receiving_personnel
		from redcap_data 
			where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'receiving_personnel';
				
		-- -----------------------------------------------------------------
		-- Find receiving lab
		-- -----------------------------------------------------------------
		select value 
		into receiving_lab
		from redcap_data 
		where project_id = new.project_id 
			and event_id = new.event_id 
			and record = new.record 
			and field_name = 'receiving_lab';
			
		-- ------------------------------------------------------------------
		-- Go through the specimen in the batch by looking for the 
		-- barcodes.
		-- ------------------------------------------------------------------
				
		open specimen;
		s1_loop: LOOP
			fetch specimen into c_project_id, c_event_id, c_record, c_field_name, c_value, c_instance;
			IF done THEN
				LEAVE s1_loop;
			END IF;
							
			-- Update the staging area
			update redcap_ddp_specimen 
			set receiving_personnel = receiving_personnel,
				receiving_date_time = received_datetime,
				RECEIVING_LAB = receiving_lab,
				dhis2_synched = false
			where specimen_barcode = c_value;
			
		END LOOP s1_loop;
		
	end if;
end; $$

DELIMITER ;

