DROP TRIGGER IF EXISTS `lab_extraction_complete_update_trigger`;

DELIMITER $$
 
create trigger lab_extraction_complete_update_trigger
after update
on redcap_data for each row
begin

	declare lab_report smallint(4);	
	declare lab_receiving smallint(4);
	declare lab_extraction smallint(4);
	declare lab_results smallint(4);
	declare report_max_event smallint(4);
	declare test_ext_personnel varchar(100);
	declare test_ext_batchsize varchar(100);
	declare test_ext_batch_id varchar(100);
	declare test_ext_datetime varchar(100);
	declare test_ext_instrument varchar(100);
	declare extraction_lab varchar(100);
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
		and field_name like 'test_ext_barcode_%';
    
	DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

	select 352 into lab_report;	
	select 362 into lab_receiving;	
	select 344 into lab_extraction;	
	select 345 into lab_results;
    
	-- Stop calling this procedure with any other data
	if (new.field_name = 'testing_extraction_complete' and new.value = '2' and new.project_id = lab_extraction) then

		select max(event_id) into report_max_event from redcap_data where project_id = lab_report;
		
		select value into test_ext_personnel
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and record = new.record
			and field_name = 'test_ext_personnel';
		
		select value into test_ext_batchsize
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and record = new.record
			and field_name = 'test_ext_batchsize';
            
		select value into test_ext_batch_id
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and record = new.record
			and field_name = 'test_ext_batch_id';
		
		select value into test_ext_datetime
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and record = new.record
			and field_name = 'test_ext_datetime';
			
		select value into extraction_lab
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and record = new.record
			and field_name = 'extraction_lab';
			
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
            
			select substring(c_field_name, 18, 2) into pos;
                                    
			select value into test_ext_instrument
			from redcap_data
			where project_id = new.project_id
				and event_id = new.event_id
				and record = new.record
				and field_name = concat('test_ext_instrument', pos);
                
            -- Update the staging area
            update redcap_ddp_specimen
            set TEST_EXTRACTION_PERSONNEL = test_ext_personnel,
				TEST_EXTRACTION_DATETIME = test_ext_datetime,
                TEST_EXTRACTION_BATCHSIZE = test_ext_batchsize,
                TEST_EXTRACTION_INSTRUMENT = test_ext_instrument,
                EXTRACTION_BATCH_POSITION = pos,
                TEST_EXTRACTION_BATCH_ID = test_ext_batch_id,
                EXTRACTION_LAB = extraction_lab
			where specimen_barcode = c_value;
			
		END LOOP s1_loop;
	end if;
end; $$

DELIMITER ;

DROP TRIGGER IF EXISTS `lab_extraction_complete_insert_trigger`;

DELIMITER $$
 
create trigger lab_extraction_complete_insert_trigger
after insert
on redcap_data for each row
begin

	declare lab_report smallint(4);	
	declare lab_receiving smallint(4);
	declare lab_extraction smallint(4);
	declare lab_results smallint(4);
	declare report_max_event smallint(4);
	declare test_ext_personnel varchar(100);
	declare test_ext_batchsize varchar(100);
	declare test_ext_batch_id varchar(100);
	declare test_ext_datetime varchar(100);
	declare test_ext_instrument varchar(100);
	declare extraction_lab varchar(100);
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
		and field_name like 'test_ext_barcode_%';
    
	DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

	select 352 into lab_report;	
	select 362 into lab_receiving;	
	select 344 into lab_extraction;	
	select 345 into lab_results;
    
	-- Stop calling this procedure with any other data
	if (new.field_name = 'testing_extraction_complete' and new.value = '2' and new.project_id = lab_extraction) then

		select max(event_id) into report_max_event from redcap_data where project_id = lab_report;
		
		select value into test_ext_personnel
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and record = new.record
			and field_name = 'test_ext_personnel';
		
		select value into test_ext_batchsize
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and record = new.record
			and field_name = 'test_ext_batchsize';
            
		select value into test_ext_batch_id
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and record = new.record
			and field_name = 'test_ext_batch_id';
		
		select value into test_ext_datetime
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and record = new.record
			and field_name = 'test_ext_datetime';
			
		select value into extraction_lab
		from redcap_data
		where project_id = new.project_id
			and event_id = new.event_id
			and record = new.record
			and field_name = 'extraction_lab';
			
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
            
			select substring(c_field_name, 18, 2) into pos;
                                    
			select value into test_ext_instrument
			from redcap_data
			where project_id = new.project_id
				and event_id = new.event_id
				and record = new.record
				and field_name = concat('test_ext_instrument', pos);
                
            -- Update the staging area
            update redcap_ddp_specimen
            set TEST_EXTRACTION_PERSONNEL = test_ext_personnel,
				TEST_EXTRACTION_DATETIME = test_ext_datetime,
                TEST_EXTRACTION_BATCHSIZE = test_ext_batchsize,
                TEST_EXTRACTION_INSTRUMENT = test_ext_instrument,
                EXTRACTION_BATCH_POSITION = pos,
                TEST_EXTRACTION_BATCH_ID = test_ext_batch_id,
                EXTRACTION_LAB = extraction_lab
			where specimen_barcode = c_value;
			
		END LOOP s1_loop;
	end if;
end; $$

DELIMITER ;
