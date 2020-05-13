DROP TRIGGER IF EXISTS `lab_tpor_complete_update_trigger`;

DELIMITER $$
 
create trigger lab_tpor_complete_update_trigger
after update
on redcap_data for each row
begin

	declare lab_report smallint(4);	
	declare lab_receiving smallint(4);
	declare lab_extraction smallint(4);
	declare lab_results smallint(4);
	declare batch_size varchar(100);
	declare report_max_event smallint(4);
	declare test_tpor_personnel varchar(100);
	declare test_tpor_batchsize varchar(100);
	declare test_tpor_datetime varchar(100);
	declare tpor_lab varchar(100);
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
		and field_name like 'test_tpor_barcode_%';
    
	DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

	select 352 into lab_report;	
	select 362 into lab_receiving;	
	select 344 into lab_extraction;	
	select 345 into lab_results;
    
    -- Stop calling this procedure with any other data
	if (new.field_name = 'sarscov2_testing_reception_complete' and new.value = '2' and new.project_id = lab_receiving) then

		select value into batch_size from redcap_data where record = new.record and field_name = 'lab_rec_batchsize';
		select max(event_id) into report_max_event from redcap_data where project_id = lab_report;
		
		select value into test_tpor_personnel
		from redcap_data
		where project_id = new.project_id--
			and event_id = new.event_id
			and record = new.record
			and field_name = 'test_tpor_personnel';
		
		select value into test_tpor_batchsize
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and record = new.record
			and field_name = 'test_tpor_batchsize';
		
		select value into test_tpor_datetime
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and record = new.record
			and field_name = 'test_tpor_datetime';
			
		select value into tpor_lab
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and record = new.record
			and field_name = 'tpor_lab';
		-- ------------------------------------------------------------------
		-- Go through the specimen in the batch by looking for the 
		-- barcodes.
		-- ------------------------------------------------------------------			
		open specimen;
		s1_loop: LOOP
			fetch specimen into c_project_id, c_event_id, c_record, c_field_name, c_value, c_instance;
			IF done THEN
				insert into my_log values (now(), 'Done already');
				LEAVE s1_loop;
			END IF;
            
			select substring(c_field_name, 19, 2) into pos;
            
            -- Update the staging area
            update redcap_ddp_specimen
            set TEST_TPOR_PERSONNEL = test_tpor_personnel,
				TEST_TPOR_DATETIME = test_tpor_datetime,
                TEST_TPOR_BATCHSIZE = test_tpor_batchsize,
                TPOR_LAB = tpor_lab,
                TEST_TPOR_ID = pos
			where specimen_barcode = c_value;
			
		END LOOP s1_loop;
		
	end if;
end; $$

DELIMITER ;

DROP TRIGGER IF EXISTS `lab_tpor_complete_insert_trigger`;

DELIMITER $$
 
create trigger lab_tpor_complete_insert_trigger
after insert
on redcap_data for each row
begin

	declare lab_report smallint(4);	
	declare lab_receiving smallint(4);
	declare lab_extraction smallint(4);
	declare lab_results smallint(4);
	declare batch_size varchar(100);
	declare report_max_event smallint(4);
	declare test_tpor_personnel varchar(100);
	declare test_tpor_batchsize varchar(100);
	declare test_tpor_datetime varchar(100);
	declare tpor_lab varchar(100);
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
		and field_name like 'test_tpor_barcode_%';
    
	DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

	select 352 into lab_report;	
	select 362 into lab_receiving;	
	select 344 into lab_extraction;	
	select 345 into lab_results;
    
	-- Stop calling this procedure with any other data
	if (new.field_name = 'sarscov2_testing_reception_complete' and new.value = '2' and new.project_id = lab_receiving) then

		select value into batch_size from redcap_data where record = new.record and field_name = 'lab_rec_batchsize';
		select max(event_id) into report_max_event from redcap_data where project_id = lab_report;
		
		select value into test_tpor_personnel
		from redcap_data
		where project_id = new.project_id--
			and event_id = new.event_id
			and record = new.record
			and field_name = 'test_tpor_personnel';
		
		select value into test_tpor_batchsize
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and record = new.record
			and field_name = 'test_tpor_batchsize';
		
		select value into test_tpor_datetime
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and record = new.record
			and field_name = 'test_tpor_datetime';
			
		select value into tpor_lab
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and record = new.record
			and field_name = 'tpor_lab';
		-- ------------------------------------------------------------------
		-- Go through the specimen in the batch by looking for the 
		-- barcodes.
		-- ------------------------------------------------------------------			
		open specimen;
		s1_loop: LOOP
			fetch specimen into c_project_id, c_event_id, c_record, c_field_name, c_value, c_instance;
			IF done THEN
				insert into my_log values (now(), 'Done already');
				LEAVE s1_loop;
			END IF;
            
			select substring(c_field_name, 19, 2) into pos;
            
            -- Update the staging area
            update redcap_ddp_specimen
            set TEST_TPOR_PERSONNEL = test_tpor_personnel,
				TEST_TPOR_DATETIME = test_tpor_datetime,
                TEST_TPOR_BATCHSIZE = test_tpor_batchsize,
                TPOR_LAB = tpor_lab,
                TEST_TPOR_ID = pos
			where specimen_barcode = c_value;
			
		END LOOP s1_loop;
		
	end if;
end; $$

DELIMITER ;

