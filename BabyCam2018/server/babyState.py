# babyState.py
# 얼굴에서 특징 뽑아내 점찍어보기 - 눈, 입 점에서 가장 위에, 가장 아래점 뽑기
# 입, 눈 크기 가져와 기준정하기

from PIL import Image, ImageDraw
import face_recognition
import cv2

def findStateForImage(imagename):
    # 기준정하기 
    smallEye = 6
    bigEye = 9
    smallLip = 55
    bigLip = 65

    # 리턴값 (평소:0, 우는:1, 자는:2)
    result =99

    # jpg파일을 numpy array로 가져옴.
    filename = imagename
    image = face_recognition.load_image_file(filename)
    cvImage = cv2.imread(filename)

    # 이미지에서 모든 얼굴을 찾고, 거기서 특징 뽑아냄.
    # face_landmarks_list[0] = 첫번째 사람 얼굴의 특징들
    # face_landmakrs_list[1] = 두번째 사람 얼굴의 특징들
    face_landmarks_list = face_recognition.face_landmarks(image)

    # 어떤상태인지 출력위한 변수
    put_str = "state"
    
    list = []
    for face_landmarks in face_landmarks_list:
        eye, lip = 0, 0
        pil_image = Image.fromarray(image)
        d = ImageDraw.Draw(pil_image, 'RGBA')
        
        # 왼쪽 눈
        #print(face_landmarks['left_eye'])
        for x, y in face_landmarks['left_eye'] :
            list.append(y)

        #print("list = ", list)

        list.sort()
        #print("list sort = ", list)
        #print("list max = %d, min = %d"%(list[len(list)-1], list[0]))
        #print("눈의 위,아래 크기 : ", list[len(list)-1]-list[0])
        eye = list[len(list)-1]-list[0]
        
        # 입(윗입술)
        #print(face_landmarks['top_lip'])
        for x, y in face_landmarks['top_lip'] :
            list.append(y)

        #print("list = ",print("list sort = ", list)
        #print("윗입술 중 가장 큰 크기 : ", list[len(list)-1])
        top_lip = list[len(list)-1]
        
        # 입(아랫입술)
        #print(face_landmarks['bottom_lip'])
        for x, y in face_landmarks['bottom_lip'] :
            list.append(y)

        #print("list = ", list)

        list.sort()
        #print("list sort = ", list)
        #print("윗입술 중 가장 작은 크기 : ", list[0])
        bottom_lip = list[0]
        
        # 입술 차이
        #print("입의 위,아래 크기 : ", top_lip-bottom_lip)
        lip = top_lip-bottom_lip

        for i in face_landmarks['left_eyebrow']:
            cv2.line(cvImage,i,(i[0]+1,i[1]+1), (0,0,255), 2)

        for i in face_landmarks['right_eyebrow']:
            cv2.line(cvImage,i,(i[0]+1,i[1]+1), (0,0,255), 2)

        for i in face_landmarks['left_eye']:
            cv2.line(cvImage,i,(i[0]+1,i[1]+1), (0,0,255), 2)

        for i in face_landmarks['right_eye']:
            cv2.line(cvImage,i,(i[0]+1,i[1]+1), (0,0,255), 2)

        for i in face_landmarks['top_lip']:
            cv2.line(cvImage,i,(i[0]+1,i[1]+1), (0,0,255), 2)

        for i in face_landmarks['bottom_lip']:
            cv2.line(cvImage,i,(i[0]+1,i[1]+1), (0,0,255), 2)
        cv2.imshow("cv : line",cvImage)


        print("==========================================================")
        print("eye : %d, lip : %d"%(eye, lip))
        print("기준 eye : %d, lip : %d"%(bigEye, bigLip))
        # 깨있을 때
        if(eye >= bigEye):
            print("눈떠있음, ", end="")
            if(lip > bigLip):
                print("입큼 : 평소상태")
                put_str = "normal"
            elif(lip < bigLip):
                print("입작음 : 평소상태")
                put_str = "normal"
            result = 0
        # 감고있을 때
        elif(eye < bigEye):
            print("눈감고있음, ", end="")
            if(lip > bigLip):
                print("입큼 : 우는상태")
                put_str = "crying"
                result = 1
            elif(lip < bigLip):
                print("입작음 : 자는상태")
                put_str = "sleeping"
                result = 0

        # 아기 상태 출력
        cv2.rectangle(cvImage, (0, 0), (150, 35), (0, 0, 255), cv2.FILLED)
        font = cv2.FONT_HERSHEY_DUPLEX
        cv2.putText(cvImage, put_str, (6, 30), font, 1.0, (255, 255, 255), 1)
        cv2.imshow("cv : line",cvImage)
        break;

    return result
