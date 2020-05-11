DROP PROCEDURE IF EXISTS labReception;
DELIMITER //

-- ----------------------------------------------------------------------
-- Procedure for responding to the receiving form
-- ----------------------------------------------------------------------
CREATE PROCEDURE labReception(
	IN project_id int(10),
	IN event_id int(10),
	IN record varchar(100),
	IN field_name varchar(100),
	IN i_value text,
	IN i_instance smallint(4)
)
BEGIN
	declare lab_report smallint(4);
	declare lab_receiving smallint(4);
	declare lab_extraction smallint(4);
	declare lab_results smallint(4);
	declare batch_size varchar(100);
	declare report_max_event smallint(4);
	declare received_datetime varchar(255);
	declare receiving_personnel varchar(255);
	declare receiving_lab varchar(255);
	declare c_project_id int(10);
	declare c_event_id int(10);
	declare c_record varchar(100);
	declare c_field_name varchar(100);
	declare c_value text;
	declare c_instance smallint(4);
    
	declare specimen cursor
	for
	select project_id, event_id, record, field_name, value, instance
	from redcap_data
	where project_id = project_id
		and event_id = event_id
		and record = record
		and field_name like 'lab_rec_barcode_%';
	-- Stop calling this procedure with any other data
	if (field_name = 'lab_reception_complete' and i_value = '2' and project_id = 362) then
		
		select 352 into lab_report;	
		select 362 into lab_receiving;	
		select 344 into lab_extraction;	
		select 345 into lab_results;

		select value into batch_size from redcap_data where record = new.record and field_name = 'lab_rec_batchsize';
		
		select max(event_id) into report_max_event from redcap_data where project_id = lab_report;
			
		-- -----------------------------------------------------------------
		-- Find receiving date time
		-- -----------------------------------------------------------------
		select value into received_datetime
		from redcap_data 
		where project_id = project_id 
			and event_id = event_id 
			and record = record 
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
		select value into receiving_lab
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
		s_loop: LOOP
			fetch specimen into c_project_id, c_event_id, c_record, c_field_name, c_value, c_instance;
			
			-- Update the staging area
			update redcap_ddp_specimen 
			set receiving_personnel = receiving_personnel,
				receiving_date_time = received_datetime,
				dhis2_synched = false
			where specimen_barcode = c_value;
			
			-- insert into the lab report
			insert into redcap_data (project_id, event_id, record, field_name, value)
			values 	(lab_report, report_max_event, c_value, c_field_name, c_value),
					(lab_report, report_max_event, c_value, 'received_datetime', received_datetime),
					(lab_report, report_max_event, c_value, 'receiving_personnel', receiving_personnel),
					(lab_report, report_max_event, c_value, 'receiving_lab', receiving_lab);
		END LOOP s_loop;
		
	end if;

END //

DELIMITER ;

